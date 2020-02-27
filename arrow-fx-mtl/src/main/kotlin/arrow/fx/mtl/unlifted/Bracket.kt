package arrow.fx.mtl.unlifted

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.internal.AtomicRefW
import arrow.core.some
import arrow.core.toT
import arrow.fx.typeclasses.Bracket
import arrow.fx.typeclasses.ExitCase
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.mtl.typeclasses.StM

fun <F, A, C, B, E> Kind<F, A>.defaultBracket(
  BR: Bracket<B, E>,
  MBC: MonadBaseControl<B, F>,
  release: (A, ExitCase<E>) -> Kind<F, Unit>,
  use: (A) -> Kind<F, C>
): Kind<F, C> = MBC.liftBaseWith { runInBase ->
  BR.run {
    val atomic: AtomicRefW<Option<StM<F, Any?>>> = AtomicRefW(None)
    runInBase(this@defaultBracket).bracketCase({ stmA, exitCase ->
      MBC.run {
        MBC.MM().run {
          when (exitCase) {
            is ExitCase.Completed ->
              // Apply monad state of use, which is guaranteed to exist because use completed
              runInBase(stmA.restoreM().flatMap { a -> atomic.value.orNull()!!.restoreM().flatMap { release(a, exitCase) } })
                .map { atomic.value = (it as StM<F, Any?>).some() }
            else ->
              runInBase(stmA.restoreM().flatMap { a -> release(a, exitCase) }).unit()
          }
        }
      }
    }, { stmA ->
      MBC.run { MBC.MM().run { runInBase(stmA.restoreM().flatMap(use)).map { it.also { atomic.value = (it as StM<F, Any?>).some() } } } }
    }).map { st ->
      atomic.value.orNull()!! toT st
    }
  }
}.let {
  MBC.MM().run {
    it.flatMap { (releaseSt, useSt) ->
      MBC.run { useSt.restoreM().flatTap { releaseSt.restoreM() } }
    }
  }
}
