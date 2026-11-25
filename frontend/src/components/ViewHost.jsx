import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import LoginView from '../views/LoginView'
import SignUpView from '../views/SignUpView'
import CarsView from '../views/CarsView'
import BookingView from '../views/BookingView'
import ListCarView from '../views/ListCarView'

/**
 * Colleague: shows the current page. The mediator tells it which page to
 * show, so the pages never link to each other directly. It also announces
 * each page that opens, so the mediator can load that page's data.
 */
export default function ViewHost() {
  const mediator = useMediator()
  const [view, setView] = useState('loading')

  useEffect(() => {
    const unregister = mediator.register('viewHost', { show: setView })
    mediator.notify('viewHost', 'app:started')
    return unregister
  }, [mediator])

  useEffect(() => {
    if (view !== 'loading') mediator.notify('viewHost', 'view:opened', { view })
  }, [mediator, view])

  switch (view) {
    case 'login':
      return <LoginView />
    case 'signUp':
      return <SignUpView />
    case 'cars':
      return <CarsView />
    case 'booking':
      return <BookingView />
    case 'listCar':
      return <ListCarView />
    default:
      return null // still checking whether the browser already has a session
  }
}
