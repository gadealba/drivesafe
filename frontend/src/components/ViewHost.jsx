import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import LoginView from '../views/LoginView'
import SignUpView from '../views/SignUpView'

/**
 * Colleague: shows the current page. The mediator tells it which page to
 * show, so the pages never link to each other directly.
 */
export default function ViewHost() {
  const mediator = useMediator()
  const [view, setView] = useState('login')

  useEffect(() => mediator.register('viewHost', { show: setView }), [mediator])

  return view === 'signUp' ? <SignUpView /> : <LoginView />
}
