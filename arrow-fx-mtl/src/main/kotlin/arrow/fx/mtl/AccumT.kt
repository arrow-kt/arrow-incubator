package arrow.fx.mtl

import arrow.Kind
import arrow.extension
import arrow.fx.IO
import arrow.fx.mtl.unlifted.defaultBracket
import arrow.fx.typeclasses.Bracket
import arrow.fx.typeclasses.ExitCase
import arrow.fx.typeclasses.MonadIO
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.mtl.extensions.AccumTMonad
import arrow.mtl.extensions.AccumTMonadError
import arrow.mtl.extensions.accumt.monadTrans.liftT
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.typeclasses.Monoid

@extension
interface AccumTMonadIO<S, F> : MonadIO<AccumTPartialOf<S, F>>, AccumTMonad<S, F> {
  fun FIO(): MonadIO<F>

  override fun MS(): Monoid<S>
  override fun MF(): Monad<F> = FIO()

  override fun <A> IO<A>.liftIO(): Kind<AccumTPartialOf<S, F>, A> = FIO().run {
    liftIO().liftT(MS(), MF())
  }
}

@extension
interface AccumTBracket<S, F, E> : Bracket<AccumTPartialOf<S, F>, E>, AccumTMonadError<S, F, E> {
  override fun ME(): MonadError<F, E> = BR()
  override fun MS(): Monoid<S>

  fun BR(): Bracket<F, E>

  override fun <A, B> Kind<AccumTPartialOf<S, F>, A>.bracketCase(release: (A, ExitCase<E>) -> Kind<AccumTPartialOf<S, F>, Unit>, use: (A) -> Kind<AccumTPartialOf<S, F>, B>): Kind<AccumTPartialOf<S, F>, B> =
    defaultBracket(BR(), AccumT.monadBaseControl(MonadBaseControl.id(BR()), MS()), release, use)
}
