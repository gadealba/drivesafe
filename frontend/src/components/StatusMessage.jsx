import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import { colleagueKey } from '../mediator/colleagueKey'

/** Colleague: shows success and error text. Only the mediator tells it what to say. */
export default function StatusMessage({ form }) {
  const mediator = useMediator()
  const [message, setMessage] = useState(null)
  const key = colleagueKey(form, 'status')

  useEffect(
    () =>
      mediator.register(key, {
        show: (type, text) => setMessage({ type, text }),
        clear: () => setMessage(null),
      }),
    [mediator, key]
  )

  return (
    <p className={`auth-status ${message?.type ?? ''}`} role="status" aria-live="polite">
      {message?.text ?? ''}
    </p>
  )
}
