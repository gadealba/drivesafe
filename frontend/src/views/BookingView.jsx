import './AuthView.css'
import './RentalViews.css'
import { useMediator } from '../mediator/useMediator'
import BookingSummary from '../components/BookingSummary'
import TextField from '../components/TextField'
import SubmitButton from '../components/SubmitButton'
import NavButton from '../components/NavButton'
import StatusMessage from '../components/StatusMessage'

/** The page opened from a car's Book button: choose pick-up and return times. */
export default function BookingView() {
  const mediator = useMediator()

  const handleSubmit = (e) => {
    e.preventDefault()
    mediator.notify('bookingView', 'form:submitted', { form: 'booking' })
  }

  return (
    <main className="auth-page">
      <h1 className="auth-title">Book a car</h1>

      <section className="auth-card auth-card--wide">
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <BookingSummary />
          <TextField form="booking" name="start" type="datetime-local" label="Pick-up date and time" />
          <TextField form="booking" name="end" type="datetime-local" label="Return date and time" />
          <SubmitButton form="booking" label="Request booking" busyLabel="Booking..." />
          <NavButton label="Back to cars" to="cars" />
          <StatusMessage form="booking" />
        </form>
      </section>
    </main>
  )
}
