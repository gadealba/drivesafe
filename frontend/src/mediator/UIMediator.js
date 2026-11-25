import { createBooking, createCar, getCars } from '../api/cars'
import {
  createUser,
  getSecurityQuestions,
  getSession,
  loginUser,
  logoutSession,
} from '../api/users'
import { formatMoney } from '../format'
import { colleagueKey } from './colleagueKey'

/**
 * Mediator for the whole UI (GoF Mediator pattern).
 *
 * Ground rules for every UI component in this project:
 *  1. Components ("colleagues") never import, reference, or call each other.
 *  2. A component gets the mediator with useMediator(), registers the small
 *     interface the mediator is allowed to call on it, and reports user
 *     actions with mediator.notify(senderName, eventName, payload).
 *  3. All behaviour that spans components, every API call, and all page
 *     navigation lives here.
 *  4. A new interaction means a new case in notify(), never a new link
 *     between two components.
 *
 * Colleagues (registered by name):
 *   viewHost                 -> { show(viewName) }
 *   <form>.<field>           -> { getValue, clear, focus }   (text and date fields)
 *   signUp.question1..3      -> { getValue, clear, setOptions(questions), setLoading(bool) }
 *   <form>.submitButton      -> { setEnabled, setBusy }
 *   <form>.status            -> { show(type, text), clear }
 *   cars.list                -> { setCars(cars), setPhase('loading' | 'ready' | 'error') }
 *   booking.summary          -> { setCar(car), setEstimate({ days, total } | null) }
 * where <form> is "signUp", "login", "booking", "listCar", or "cars" (status only).
 *
 * Events: app:started, view:opened {view}, field:changed {form}, form:submitted {form},
 *         navigate {to}, car:selected {carId}, logout:requested.
 */

// How many security questions a new user answers. Must match
// REQUIRED_ANSWER_COUNT in the back end (SecurityAnswerService).
const SLOTS = [1, 2, 3]
const questionName = (n) => `question${n}`
const answerName = (n) => `answer${n}`

const FORMS = {
  // gateSubmit: keep the submit button disabled until every field has a value.
  signUp: {
    fields: [
      'username',
      'password',
      'email',
      ...SLOTS.flatMap((n) => [questionName(n), answerName(n)]),
    ],
    gateSubmit: true,
  },
  // Not gated, because browsers and password managers can autofill without typing.
  login: { fields: ['username', 'password'], gateSubmit: false },
  booking: { fields: ['start', 'end'], gateSubmit: true },
  listCar: {
    fields: [
      'year',
      'mileage',
      'make',
      'model',
      'pickupLocation',
      'pricePerDay',
      'availableFrom',
      'availableUntil',
    ],
    gateSubmit: true,
  },
}
const VIEWS = ['login', 'signUp', 'cars', 'booking', 'listCar']
const VIEWS_NEEDING_LOGIN = ['cars', 'booking', 'listCar']

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MIN_PASSWORD_LENGTH = 8
const MS_PER_DAY = 24 * 60 * 60 * 1000

export class UIMediator {
  constructor() {
    this.colleagues = {}
    this.submitting = new Set()

    this.currentUser = null // { id, username } while a session is open
    this.pendingNotice = null // message to show on the login page

    this.questions = null // cached security questions
    this.loadingQuestions = false

    this.cars = [] // cars shown on the cars page
    this.selectedCar = null // car chosen for booking
  }

  /** A colleague hands the mediator its interface. Returns an unregister function. */
  register(name, colleague) {
    this.colleagues[name] = colleague

    // A submit button that just appeared needs its initial enabled state.
    const [form, role] = name.split('.')
    if (role === 'submitButton') this.refreshSubmitButton(form)

    return () => {
      if (this.colleagues[name] === colleague) delete this.colleagues[name]
    }
  }

  /** A colleague reports that something happened. */
  notify(sender, event, payload) {
    switch (event) {
      case 'app:started':
        return this.handleAppStarted()
      case 'view:opened':
        return this.handleViewOpened(payload.view)
      case 'field:changed':
        return this.handleFieldChanged(payload.form)
      case 'form:submitted':
        return this.handleSubmit(payload.form)
      case 'navigate':
        return this.handleNavigate(payload.to)
      case 'car:selected':
        return this.handleCarSelected(payload.carId)
      case 'logout:requested':
        return this.handleLogout()
      default:
        console.warn(`UIMediator: unhandled event "${event}" from "${sender}"`)
    }
  }

  // ---- session and navigation --------------------------------------------

  /** On page load: if the browser still has a valid session cookie, skip the login page. */
  async handleAppStarted() {
    try {
      const session = await getSession()
      this.currentUser = { id: session.userId, username: session.username }
      this.show('cars')
    } catch {
      this.show('login')
    }
  }

  /** Both signing up and logging in open a session on the server. */
  openSession(user) {
    this.currentUser = { id: user.id, username: user.username }
    this.show('cars')
  }

  async handleLogout() {
    try {
      await logoutSession()
    } catch {
      // Even if the request fails, leave the logged-in pages.
    }
    this.endLocalSession()
    this.show('login')
  }

  endLocalSession() {
    this.currentUser = null
    this.cars = []
    this.selectedCar = null
  }

  /** Returns true when the error means the session is gone (and sends the user to login). */
  handleSessionExpired(err) {
    if (err.status !== 401) return false
    this.endLocalSession()
    this.pendingNotice = 'Your session has ended. Please log in again.'
    this.show('login')
    return true
  }

  show(view) {
    this.colleagues.viewHost?.show(view)
  }

  handleNavigate(to) {
    if (!VIEWS.includes(to)) {
      console.warn(`UIMediator: unknown view "${to}"`)
      return
    }
    if (VIEWS_NEEDING_LOGIN.includes(to) && !this.currentUser) {
      this.show('login')
      return
    }
    if (to === 'booking' && !this.selectedCar) return
    this.show(to)
  }

  handleViewOpened(view) {
    switch (view) {
      case 'signUp':
        return this.loadQuestions()
      case 'cars':
        return this.loadCars()
      case 'booking':
        this.find('booking', 'summary')?.setCar(this.selectedCar)
        return
      case 'login':
        return this.showPendingNotice()
    }
  }

  showPendingNotice() {
    if (!this.pendingNotice) return
    this.find('login', 'status')?.show('error', this.pendingNotice)
    this.pendingNotice = null
  }

  // ---- cars ---------------------------------------------------------------

  async loadCars() {
    const list = () => this.find('cars', 'list')
    list()?.setPhase('loading')
    try {
      const all = await getCars()
      // Owners can't rent their own cars, so don't offer them.
      this.cars = all.filter((car) => car.ownerId !== this.currentUser?.id)
      list()?.setCars(this.cars)
      list()?.setPhase('ready')
    } catch (err) {
      if (this.handleSessionExpired(err)) return
      list()?.setCars([])
      list()?.setPhase('error')
      this.find('cars', 'status')?.show('error', `Could not load cars: ${err.message}`)
    }
  }

  handleCarSelected(carId) {
    const car = this.cars.find((c) => c.id === carId)
    if (!car) return
    if (car.ownerId === this.currentUser?.id) return // owners can't rent their own cars
    this.selectedCar = car
    this.show('booking')
  }

  /** Shows the estimated total under the car details while the dates are being picked. */
  refreshBookingEstimate() {
    const summary = this.find('booking', 'summary')
    if (!summary) return

    const { start, end } = this.readValues('booking')
    const from = Date.parse(start)
    const to = Date.parse(end)
    if (!this.selectedCar || Number.isNaN(from) || Number.isNaN(to) || to <= from) {
      summary.setEstimate(null)
      return
    }
    const days = Math.max(1, Math.ceil((to - from) / MS_PER_DAY))
    summary.setEstimate({ days, total: this.selectedCar.pricePerDay * days })
  }

  // ---- forms --------------------------------------------------------------

  handleFieldChanged(form) {
    this.find(form, 'status')?.clear()
    this.refreshSubmitButton(form)
    // Choosing a question removes it from the other two dropdowns.
    if (form === 'signUp') this.refreshQuestionOptions()
    if (form === 'booking') this.refreshBookingEstimate()
  }

  async handleSubmit(form) {
    if (!FORMS[form] || this.submitting.has(form)) return

    const values = this.readValues(form)
    const problem = this.validate(form, values)
    if (problem) {
      this.find(form, 'status')?.show('error', problem)
      return
    }

    this.submitting.add(form)
    this.find(form, 'submitButton')?.setBusy(true)
    try {
      if (form === 'signUp') await this.completeSignUp(values)
      if (form === 'login') await this.completeLogin(values)
      if (form === 'booking') await this.completeBooking(values)
      if (form === 'listCar') await this.completeListing(values)
    } catch (err) {
      if ((form === 'booking' || form === 'listCar') && this.handleSessionExpired(err)) return
      this.find(form, 'status')?.show('error', err.message)
    } finally {
      this.submitting.delete(form)
      this.find(form, 'submitButton')?.setBusy(false)
    }
  }

  async completeSignUp(values) {
    const securityAnswers = SLOTS.map((n) => ({
      questionId: Number(values[questionName(n)]),
      answer: values[answerName(n)].trim(),
    }))

    const user = await createUser({
      username: values.username.trim(),
      password: values.password,
      email: values.email.trim(),
      securityAnswers,
    })
    this.openSession(user)
  }

  async completeLogin(values) {
    const user = await loginUser({
      username: values.username.trim(),
      password: values.password,
    })
    this.openSession(user)
  }

  async completeBooking(values) {
    const booking = await createBooking(this.selectedCar.id, {
      start: new Date(values.start).toISOString(),
      end: new Date(values.end).toISOString(),
    })
    this.clearFields('booking')
    this.refreshSubmitButton('booking')
    this.find('booking', 'summary')?.setEstimate(null)
    this.find('booking', 'status')?.show(
      'success',
      `Booking confirmed! Total: ${formatMoney(booking.totalPrice)}`
    )
  }

  /** The owner's new listing: only the details the project needs, plus one availability window. */
  async completeListing(values) {
    const car = await createCar({
      year: Number(values.year),
      make: values.make.trim(),
      model: values.model.trim(),
      mileage: Number(values.mileage),
      pickupLocation: values.pickupLocation.trim(),
      pricePerDay: Number(values.pricePerDay),
      availability: [
        {
          start: new Date(values.availableFrom).toISOString(),
          end: new Date(values.availableUntil).toISOString(),
        },
      ],
    })
    this.clearFields('listCar')
    this.refreshSubmitButton('listCar')
    this.find('listCar', 'status')?.show(
      'success',
      `Listed! Your ${car.year} ${car.make} ${car.model} is now available for rent.`
    )
    this.find('listCar', 'year')?.focus()
  }

  // ---- security question dropdowns ---------------------------------------

  /** Loads the questions once, then hands them to the dropdowns. */
  async loadQuestions() {
    if (this.questions) {
      this.refreshQuestionOptions()
      return
    }
    if (this.loadingQuestions) return

    this.loadingQuestions = true
    this.setQuestionLoading(true)
    try {
      this.questions = await getSecurityQuestions()
      this.refreshQuestionOptions()
    } catch (err) {
      this.find('signUp', 'status')?.show(
        'error',
        `Could not load security questions (${err.message}). Go back to login and try again.`
      )
    } finally {
      this.loadingQuestions = false
      this.setQuestionLoading(false)
    }
  }

  setQuestionLoading(isLoading) {
    SLOTS.forEach((n) => this.find('signUp', questionName(n))?.setLoading(isLoading))
  }

  /**
   * Gives each dropdown every question except the ones already chosen in the
   * other dropdowns, so the same question can't be picked twice.
   */
  refreshQuestionOptions() {
    if (!this.questions) return
    const chosen = SLOTS.map((n) => this.find('signUp', questionName(n))?.getValue() ?? '')

    SLOTS.forEach((n, i) => {
      const takenByOthers = new Set(chosen.filter((id, j) => j !== i && id !== ''))
      const available = this.questions.filter((q) => !takenByOthers.has(String(q.id)))
      this.find('signUp', questionName(n))?.setOptions(available)
    })
  }

  // ---- helpers ------------------------------------------------------------

  find(form, name) {
    return this.colleagues[colleagueKey(form, name)]
  }

  /** Asks each field colleague of a form for its current value. */
  readValues(form) {
    const values = {}
    for (const name of FORMS[form].fields) {
      values[name] = this.find(form, name)?.getValue() ?? ''
    }
    return values
  }

  clearFields(form) {
    FORMS[form].fields.forEach((name) => this.find(form, name)?.clear())
  }

  refreshSubmitButton(form) {
    const config = FORMS[form]
    if (!config) return
    const values = this.readValues(form)
    const allFilled = config.fields.every((name) => values[name].trim() !== '')
    this.find(form, 'submitButton')?.setEnabled(!config.gateSubmit || allFilled)
  }

  /** Returns an error message, or null when the input is acceptable. */
  validate(form, values) {
    if (form === 'login') {
      if (!values.username.trim() || !values.password) {
        return 'Enter your username and password.'
      }
      return null
    }

    if (form === 'booking') {
      const start = Date.parse(values.start)
      const end = Date.parse(values.end)
      if (Number.isNaN(start) || Number.isNaN(end)) {
        return 'Choose a pick-up and a return date and time.'
      }
      if (end <= start) return 'The return time must be after the pick-up time.'
      if (start < Date.now()) return "The pick-up time can't be in the past."
      return null
    }

    if (form === 'listCar') {
      const year = Number(values.year)
      const latestYear = new Date().getFullYear() + 1
      if (!Number.isInteger(year) || year < 1950 || year > latestYear) {
        return `Year must be between 1950 and ${latestYear}.`
      }
      if (!values.make.trim()) return 'Please enter the make.'
      if (!values.model.trim()) return 'Please enter the model.'
      const mileage = Number(values.mileage)
      if (!Number.isInteger(mileage) || mileage < 0) {
        return 'Mileage must be a whole number, 0 or more.'
      }
      if (!values.pickupLocation.trim()) return 'Please enter a pickup location.'
      if (!(Number(values.pricePerDay) > 0)) return 'Price per day must be greater than 0.'

      const from = Date.parse(values.availableFrom)
      const until = Date.parse(values.availableUntil)
      if (Number.isNaN(from) || Number.isNaN(until)) {
        return 'Choose when the car is available from and until.'
      }
      if (until <= from) return 'The availability must end after it starts.'
      if (until <= Date.now()) return 'The availability must end in the future.'
      return null
    }

    if (!values.username.trim()) return 'Please enter a username.'
    if (!EMAIL_PATTERN.test(values.email.trim())) return 'Please enter a valid email address.'
    if (values.password.length < MIN_PASSWORD_LENGTH) {
      return `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`
    }

    const chosen = SLOTS.map((n) => values[questionName(n)])
    if (chosen.some((id) => id === '')) {
      return 'Please choose a security question for each answer.'
    }
    if (new Set(chosen).size !== chosen.length) {
      return 'Please choose different security questions.'
    }
    if (SLOTS.some((n) => !values[answerName(n)].trim())) {
      return 'Please answer each security question.'
    }
    return null
  }
}
