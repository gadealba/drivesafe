import { useMediator } from '../mediator/useMediator'

/**
 * Colleague: a button that asks to go to another page. It does not know how
 * navigation works or what the other page is. It only reports the request.
 */
export default function NavButton({ label, to }) {
  const mediator = useMediator()

  return (
    <button
      type="button"
      className="auth-button auth-button--secondary"
      onClick={() => mediator.notify('navButton', 'navigate', { to })}
    >
      {label}
    </button>
  )
}
