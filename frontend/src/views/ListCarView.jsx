import './AuthView.css'
import './RentalViews.css'
import './ListingViews.css'
import { useMediator } from '../mediator/useMediator'
import TextField from '../components/TextField'
import SubmitButton from '../components/SubmitButton'
import NavButton from '../components/NavButton'
import StatusMessage from '../components/StatusMessage'

/** The page where an owner lists a car for rent. */
export default function ListCarView() {
  const mediator = useMediator()

  const handleSubmit = (e) => {
    e.preventDefault()
    mediator.notify('listCarView', 'form:submitted', { form: 'listCar' })
  }

  return (
    <main className="auth-page">
      <h1 className="auth-title">List your vehicle</h1>

      <section className="auth-card auth-card--form">
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="auth-row">
            <TextField form="listCar" name="year" type="number" label="Year" inputProps={{ min: 1950, step: 1 }} />
            <TextField form="listCar" name="mileage" type="number" label="Mileage" inputProps={{ min: 0, step: 1 }} />
          </div>
          <div className="auth-row">
            <TextField form="listCar" name="make" label="Make" />
            <TextField form="listCar" name="model" label="Model" />
          </div>
          <TextField form="listCar" name="pickupLocation" label="Pickup location" />
          <TextField form="listCar" name="pricePerDay" type="number" label="Price per day (USD)" inputProps={{ min: 0, step: 0.01 }} />
          <TextField form="listCar" name="availableFrom" type="datetime-local" label="Available from" />
          <TextField form="listCar" name="availableUntil" type="datetime-local" label="Available until" />

          <SubmitButton form="listCar" label="List my vehicle" busyLabel="Listing..." />
          <NavButton label="Back to cars" to="cars" />
          <StatusMessage form="listCar" />
        </form>
      </section>
    </main>
  )
}
