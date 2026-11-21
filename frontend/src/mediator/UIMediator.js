import { createUser, loginUser } from '../api/users'
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
 *   <form>.<field>                  -> { getValue, clear, focus }
 *   <form>.submitButton             -> { setEnabled, setBusy }
 *   <form>.status                   -> { show(type, text), clear }
 * where <form> is "signUp" or "login".
 *
 * Events: field:changed {form}, form:submitted {form}, navigate {to}.
 */

const FORMS = {
  // gateSubmit: keep the submit button disabled until every field has text.
  signUp: { fields: ['username', 'password', 'email'], gateSubmit: true },
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
      default:
        console.warn(`UIMediator: unhandled event "${event}" from "${sender}"`)
    }
  }

  // ---- coordination logic -------------------------------------------------

  handleFieldChanged(form) {
    this.find(form, 'status')?.clear()
    this.refreshSubmitButton(form)
  }

  handleNavigate(to) {
    if (!VIEWS.includes(to)) {
      console.warn(`UIMediator: unknown view "${to}"`)
      return
    }
    this.colleagues.viewHost?.show(to)
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
    const user = await createUser({
      username: values.username.trim(),
      password: values.password,
      email: values.email.trim(),
    })
    this.clearFields('signUp')
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
    return null
  }
}
