import './AuthView.css'
import { useMediator } from '../mediator/useMediator'
import TextField from '../components/TextField'
import SubmitButton from '../components/SubmitButton'
import NavButton from '../components/NavButton'
import StatusMessage from '../components/StatusMessage'

/** The Sign up page: a title near the top and a centered square holding the form. */
export default function SignUpView() {
  const mediator = useMediator()

  const handleSubmit = (e) => {
    e.preventDefault()
    mediator.notify('signUpView', 'form:submitted', { form: 'signUp' })
  }

  return (
    <main className="auth-page">
      <h1 className="auth-title">Sign up</h1>

      <section className="auth-card auth-card--square">
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <TextField form="signUp" name="username" placeholder="Username" autoComplete="username" />
          <TextField form="signUp" name="password" type="password" placeholder="Password" autoComplete="new-password" />
          <TextField form="signUp" name="email" type="email" placeholder="Email" autoComplete="email" />
          <SubmitButton form="signUp" label="Create account" busyLabel="Creating account..." />
          <NavButton label="Back to login" to="login" />
          <StatusMessage form="signUp" />
        </form>
      </section>
    </main>
  )
}
