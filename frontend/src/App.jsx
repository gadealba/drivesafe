import { MediatorProvider } from './mediator/MediatorProvider'
import ViewHost from './components/ViewHost'

export default function App() {
  return (
    <MediatorProvider>
      <ViewHost />
    </MediatorProvider>
  )
}
