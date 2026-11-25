import { useMediator } from '../mediator/useMediator'

/** Colleague: a button that just reports an event. What happens next is the mediator's business. */
export default function ActionButton({ label, event, payload, className = '' }) {
  const mediator = useMediator()

  return (
    <button
      type="button"
      className={`auth-button auth-button--secondary ${className}`}
      onClick={() => mediator.notify('actionButton', event, payload)}
    >
      {label}
    </button>
  )
}
