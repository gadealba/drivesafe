import { useContext } from 'react'
import { MediatorContext } from './mediatorContext'

export function useMediator() {
  const mediator = useContext(MediatorContext)
  if (!mediator) throw new Error('useMediator must be used inside <MediatorProvider>')
  return mediator
}
