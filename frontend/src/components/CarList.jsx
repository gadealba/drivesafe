import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import CarCell from './CarCell'

/** Colleague: the list of cars. The mediator supplies the cars and says when it is loading. */
export default function CarList() {
  const mediator = useMediator()
  const [cars, setCars] = useState([])
  const [phase, setPhase] = useState('loading')

  useEffect(
    () => mediator.register('cars.list', { setCars, setPhase }),
    [mediator]
  )

  if (phase === 'error') return null
  if (phase === 'loading' && cars.length === 0) {
    return <p className="car-list__empty">Loading cars...</p>
  }
  if (cars.length === 0) {
    return <p className="car-list__empty">No cars are available for rent right now.</p>
  }

  return (
    <ul className="car-list">
      {cars.map((car) => (
        <CarCell key={car.id} car={car} />
      ))}
    </ul>
  )
}
