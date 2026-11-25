import { useEffect, useRef } from 'react'
import { useMediator } from '../mediator/useMediator'
import { colleagueKey } from '../mediator/colleagueKey'

/**
 * Colleague: one input (text, password, email, number, date...). It registers as
 * "<form>.<name>" and reports changes. It knows nothing about the other fields,
 * the buttons, or the API. Pass `label` to show a visible label, and `inputProps`
 * for extra input attributes such as min and step.
 */
export default function TextField({
  form,
  name,
  type = 'text',
  placeholder,
  label,
  autoComplete,
  inputProps,
}) {
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

  const input = (
    <input
      {...inputProps}
      ref={inputRef}
      className="auth-input"
      name={name}
      type={type}
      placeholder={placeholder}
      aria-label={label ?? placeholder}
      autoComplete={autoComplete}
      onChange={() => mediator.notify(key, 'field:changed', { form })}
    />
  )

  if (!label) return input
  return (
    <label className="auth-field">
      <span className="auth-field__label">{label}</span>
      {input}
    </label>
  )
}
