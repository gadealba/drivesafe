import { useMediator } from '../mediator/useMediator'
import { formatMoney } from '../format'

/** Colleague: one car in the list. Its button only reports which car was picked. */
export default function CarCell({ car }) {
  const mediator = useMediator()

  return (
    <li className="car-cell">
      <div className="car-cell__info">
        <span className="car-cell__title">
          {car.year} {car.make} {car.model}
        </span>
        <span className="car-cell__price">{formatMoney(car.pricePerDay)} per day</span>
      </div>
      <button
        type="button"
        className="auth-button"
        onClick={() => mediator.notify('carCell', 'car:selected', { carId: car.id })}
      >
        Book
      </button>
    </li>
  )
}
