import { useEffect, useRef, useState } from 'react'
import { useMediator } from '../mediator/useMediator'
import { colleagueKey } from '../mediator/colleagueKey'

/**
 * Colleague: a dropdown of security questions. The mediator decides which
 * questions it may offer (setOptions) and when it is loading (setLoading).
 * It knows nothing about the other dropdowns, the answer fields, or the API.
 */
export default function QuestionSelect({ form, name, label }) {
  const mediator = useMediator()
  const selectRef = useRef(null)
  const [options, setOptions] = useState([])
  const [loading, setLoading] = useState(false)
  const key = colleagueKey(form, name)

  useEffect(
    () =>
      mediator.register(key, {
        getValue: () => selectRef.current?.value ?? '',
        clear: () => {
          if (selectRef.current) selectRef.current.value = ''
        },
        setOptions,
        setLoading,
      }),
    [mediator, key]
  )

  return (
    <select
      ref={selectRef}
      className="auth-select"
      name={name}
      aria-label={label}
      defaultValue=""
      disabled={loading}
      onChange={() => mediator.notify(key, 'field:changed', { form })}
    >
      <option value="">{loading ? 'Loading questions...' : label}</option>
      {options.map((q) => (
        <option key={q.id} value={q.id}>
          {q.text}
        </option>
      ))}
    </select>
  )
}
