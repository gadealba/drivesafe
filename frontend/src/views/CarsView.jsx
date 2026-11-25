import './AuthView.css'
import './RentalViews.css'
import './ListingViews.css'
import CarList from '../components/CarList'
import StatusMessage from '../components/StatusMessage'
import ActionButton from '../components/ActionButton'

/** The page shown once a session is open: a centered rectangle listing the cars available for rent. */
export default function CarsView() {
  return (
    <main className="auth-page">
      <h1 className="auth-title">Available cars</h1>

      <div className="cars-stack">
        <div className="cars-corner">
          <ActionButton
            label="Have your vehicle rented"
            event="navigate"
            payload={{ to: 'listCar' }}
            className="auth-button--small"
          />
        </div>

        <section className="cars-panel" aria-label="Cars available for rent">
          <CarList />
          <StatusMessage form="cars" />
        </section>

        <div className="cars-stack__footer">
          <ActionButton label="Log out" event="logout:requested" className="auth-button--small" />
        </div>
      </div>
    </main>
  )
}
