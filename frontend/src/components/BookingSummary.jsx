import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import { formatMoney } from '../format'

/** Colleague: shows the car being booked and an estimated total. The mediator feeds it both. */
export default function BookingSummary() {
  const mediator = useMediator()
  const [car, setCar] = useState(null)
  const [estimate, setEstimate] = useState(null)

  useEffect(
    () => mediator.register('booking.summary', { setCar, setEstimate }),
    [mediator]
  )

  if (!car) return null
  return (
    <div className="booking-summary">
      <p className="booking-summary__title">
        {car.year} {car.make} {car.model}
      </p>
      <p>{formatMoney(car.pricePerDay)} per day</p>
      <p>Pick-up: {car.pickupLocation}</p>
      {estimate && (
        <p className="booking-summary__estimate">
          Estimated total: {formatMoney(estimate.total)} for {estimate.days}{' '}
          {estimate.days === 1 ? 'day' : 'days'}
        </p>
      )}
    </div>
  )
}
