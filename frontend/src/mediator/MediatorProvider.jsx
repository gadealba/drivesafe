import { useState } from 'react'
import { UIMediator } from './UIMediator'
import { MediatorContext } from './mediatorContext'

/** Creates one UIMediator and hands the same instance to every component below it. */
export function MediatorProvider({ children }) {
  const [mediator] = useState(() => new UIMediator())
  return <MediatorContext.Provider value={mediator}>{children}</MediatorContext.Provider>
}
