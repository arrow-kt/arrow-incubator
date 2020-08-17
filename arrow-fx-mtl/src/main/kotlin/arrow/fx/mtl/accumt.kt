package arrow.fx.mtl

import arrow.Kind
import arrow.core.AndThen
import arrow.core.toT
import arrow.extension
import arrow.fx.IO
import arrow.fx.mtl.unlifted.defaultBracket
import arrow.fx.typeclasses.Async
import arrow.fx.typeclasses.Bracket
import arrow.fx.typeclasses.ExitCase
import arrow.fx.typeclasses.MonadDefer
import arrow.fx.typeclasses.MonadIO
import arrow.fx.typeclasses.ProcF
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.mtl.extensions.AccumTMonad
import arrow.mtl.extensions.AccumTMonadError
import arrow.mtl.extensions.accumt.monadTrans.liftT
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.fix
import arrow.mtl.run
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.typeclasses.Monoid
import arrow.undocumented
import kotlin.coroutines.CoroutineContext

@extension
@undocumented
interface AccumTBracket<S, F, E> : Bracket<AccumTPartialOf<S, F>, E>, AccumTMonadError<S, F, E> {
  fun BF(): Bracket<F, E>
  override fun ME(): MonadError<F, E> = BF()
  override fun MS(): Monoid<S>
  override fun <A, B> Kind<AccumTPartialOf<S, F>, A>.bracketCase(release: (A, ExitCase<E>) -> Kind<AccumTPartialOf<S, F>, Unit>, use: (A) -> Kind<AccumTPartialOf<S, F>, B>): Kind<AccumTPartialOf<S, F>, B> =
    defaultBracket(BF(), AccumT.monadBaseControl<S, F, F>(MonadBaseControl.id(ME()), MS()), release, use)
}

@extension
@undocumented
interface AccumTMonadDefer<S, F> : MonadDefer<AccumTPartialOf<S, F>>, AccumTBracket<S, F, Throwable> {
  fun MDF(): MonadDefer<F>
  override fun BF(): Bracket<F, Throwable> = MDF()
  override fun MS(): Monoid<S>
  override fun <A> defer(fa: () -> Kind<AccumTPartialOf<S, F>, A>): Kind<AccumTPartialOf<S, F>, A> =
    AccumT { s -> MDF().defer { fa().fix().runAccumT(s) } }
}

@extension
@undocumented
interface AccumTAsync<S, F> : Async<AccumTPartialOf<S, F>>, AccumTMonadDefer<S, F> {
  fun AS(): Async<F>
  override fun MDF(): MonadDefer<F> = AS()
  override fun MS(): Monoid<S>

  override fun <A> asyncF(k: ProcF<AccumTPartialOf<S, F>, A>): Kind<AccumTPartialOf<S, F>, A> =
    AccumT { s ->
      AS().run { asyncF<A> { cb -> k(cb).fix().runAccumT(s).map { it.b } }.map { s toT it } }
    }

  override fun <A> Kind<AccumTPartialOf<S, F>, A>.continueOn(ctx: CoroutineContext): Kind<AccumTPartialOf<S, F>, A> =
    AccumT(AndThen(fix().accumT).andThen { AS().run { it.continueOn(ctx) } })
}

@extension
@undocumented
interface AccumTMonadIO<S, F> : MonadIO<AccumTPartialOf<S, F>>, AccumTMonad<S, F> {
  override fun MF(): Monad<F> = FIO()
  override fun MS(): Monoid<S>
  fun FIO(): MonadIO<F>
  override fun <A> IO<A>.liftIO(): Kind<AccumTPartialOf<S, F>, A> = FIO().run {
    liftIO().liftT(MS(), MF())
  }
}
