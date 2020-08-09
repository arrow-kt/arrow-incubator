package arrow.android.demo.cases.login

import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.concurrent.SignallingAtomic
import arrow.fx.coroutines.stream.flatten

// TODO(pabs): Move to fx-coroutines
class StateMachine<S, E>(initial: S, private val transition: (S, E) -> Stream<S>) {

  private val currentState: SignallingAtomic<S> = SignallingAtomic.unsafe(initial)

  suspend fun start(events: Stream<E>, render: (S) -> Unit): Stream<S> =
    start(events).effectTap { render(it) }
  
  suspend fun start(events: Stream<E>): Stream<S> =
    currentState
      .discrete()
      .zipWith(events, transition)
      .flatten()
      .effectTap { currentState.set(it) }

}
