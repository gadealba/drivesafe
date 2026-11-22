import { createUser, getSecurityQuestions, loginUser } from '../api/users'
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
 *   viewHost                        -> { show(viewName) }
 *   <form>.<field>                  -> { getValue, clear, focus }       (text fields)
 *   signUp.question1..3             -> { getValue, clear, setOptions(questions), setLoading(bool) }
 *   <form>.submitButton             -> { setEnabled, setBusy }
 *   <form>.status                   -> { show(type, text), clear }
 * where <form> is "signUp" or "login".
 *
 * Events: field:changed {form}, form:submitted {form}, navigate {to}, view:opened {view}.
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
}
const VIEWS = ['login', 'signUp']

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MIN_PASSWORD_LENGTH = 8

export class UIMediator {
  constructor() {
    this.colleagues = {}
    this.submitting = new Set()
    this.currentUser = null
    this.questions = null // cached list from the back end
    this.loadingQuestions = false
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
      case 'field:changed':
        return this.handleFieldChanged(payload.form)
      case 'form:submitted':
        return this.handleSubmit(payload.form)
      case 'navigate':
        return this.handleNavigate(payload.to)
      case 'view:opened':
        return this.handleViewOpened(payload.view)
      default:
        console.warn(`UIMediator: unhandled event "${event}" from "${sender}"`)
    }
  }

  // ---- coordination logic -------------------------------------------------

  handleFieldChanged(form) {
    this.find(form, 'status')?.clear()
    this.refreshSubmitButton(form)
    // Choosing a question removes it from the other two dropdowns.
    if (form === 'signUp') this.refreshQuestionOptions()
  }

  handleNavigate(to) {
    if (!VIEWS.includes(to)) {
      console.warn(`UIMediator: unknown view "${to}"`)
      return
    }
    this.colleagues.viewHost?.show(to)
  }

  handleViewOpened(view) {
    if (view === 'signUp') return this.loadQuestions()
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
    } catch (err) {
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

    this.clearFields('signUp')
    this.refreshQuestionOptions() // every dropdown offers every question again
    this.refreshSubmitButton('signUp')
    this.find('signUp', 'status')?.show('success', `Account created. Welcome, ${user.username}!`)
    this.find('signUp', 'username')?.focus()
  }

  async completeLogin(values) {
    const user = await loginUser({
      username: values.username.trim(),
      password: values.password,
    })
    this.currentUser = user
    this.clearFields('login')
    this.find('login', 'status')?.show('success', `Welcome back, ${user.username}!`)
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
