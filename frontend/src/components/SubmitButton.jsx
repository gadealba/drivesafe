import { useEffect, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import { colleagueKey } from '../mediator/colleagueKey'

/** Colleague: a form's submit button. The mediator decides when it is enabled or busy. */
export default function SubmitButton({ form, label, busyLabel = 'Please wait...' }) {
  const mediator = useMediator()
  const [enabled, setEnabled] = useState(false)
  const [busy, setBusy] = useState(false)
  const key = colleagueKey(form, 'submitButton')

  useEffect(
    () => mediator.register(key, { setEnabled, setBusy }),
    [mediator, key]
  )

  return (
    <button type="submit" className="auth-button" disabled={!enabled || busy}>
      {busy ? busyLabel : label}
    </button>
  )
}
