import { useEffect, useRef } from 'react'
import { useMediator } from '../mediator/useMediator'
import { colleagueKey } from '../mediator/colleagueKey'

/**
 * Colleague: one text input. It registers as "<form>.<name>" and reports
 * changes. It knows nothing about the other fields, the buttons, or the API.
 */
export default function TextField({ form, name, type = 'text', placeholder, autoComplete }) {
  const mediator = useMediator()
  const inputRef = useRef(null)
  const key = colleagueKey(form, name)

  useEffect(
    () =>
      mediator.register(key, {
        getValue: () => inputRef.current?.value ?? '',
        clear: () => {
          if (inputRef.current) inputRef.current.value = ''
        },
        focus: () => inputRef.current?.focus(),
      }),
    [mediator, key]
  )

  return (
    <input
      ref={inputRef}
      className="auth-input"
      name={name}
      type={type}
      placeholder={placeholder}
      aria-label={placeholder}
      autoComplete={autoComplete}
      onChange={() => mediator.notify(key, 'field:changed', { form })}
    />
  )
}
