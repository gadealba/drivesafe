import './AuthView.css'
import { useMediator } from '../mediator/useMediator'
import TextField from '../components/TextField'
import SubmitButton from '../components/SubmitButton'
import NavButton from '../components/NavButton'
import StatusMessage from '../components/StatusMessage'

/** The Login page: a title near the top and a centered rectangle holding the form. */
export default function LoginView() {
  const mediator = useMediator()

  const handleSubmit = (e) => {
    e.preventDefault()
    mediator.notify('loginView', 'form:submitted', { form: 'login' })
  }

  return (
    <main className="auth-page">
      <h1 className="auth-title">Login</h1>

      <section className="auth-card auth-card--wide">
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <TextField form="login" name="username" placeholder="Username" autoComplete="username" />
          <TextField form="login" name="password" type="password" placeholder="Password" autoComplete="current-password" />
          <SubmitButton form="login" label="Log in" busyLabel="Logging in..." />
          <NavButton label="Create an account" to="signUp" />
          <StatusMessage form="login" />
        </form>
      </section>
    </main>
  )
}
