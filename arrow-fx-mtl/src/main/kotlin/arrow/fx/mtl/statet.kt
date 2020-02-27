package arrow.fx.mtl

import arrow.Kind
import arrow.core.AndThen
import arrow.core.Tuple2
import arrow.extension
import arrow.fx.IO
import arrow.fx.mtl.unlifted.defaultBracket
import arrow.fx.typeclasses.Async
import arrow.fx.typeclasses.Bracket
import arrow.fx.typeclasses.ExitCase
import arrow.fx.typeclasses.MonadDefer
import arrow.fx.typeclasses.MonadIO
import arrow.fx.typeclasses.Proc
import arrow.fx.typeclasses.ProcF
import arrow.mtl.StateT
import arrow.mtl.StateTOf
import arrow.mtl.StateTPartialOf
import arrow.mtl.extensions.StateTMonad
import arrow.mtl.extensions.StateTMonadError
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.fix
import arrow.mtl.run
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.undocumented
import kotlin.coroutines.CoroutineContext

@extension
@undocumented
interface StateTBracket<S, F, E> : Bracket<StateTPartialOf<S, F>, E>, StateTMonadError<S, F, E> {

  fun BR(): Bracket<F, E>

  override fun ME(): MonadError<F, E> = BR()

  override fun <A, B> Kind<StateTPartialOf<S, F>, A>.bracketCase(release: (A, ExitCase<E>) -> Kind<StateTPartialOf<S, F>, Unit>, use: (A) -> Kind<StateTPartialOf<S, F>, B>): Kind<StateTPartialOf<S, F>, B> =
    defaultBracket(BR(), StateT.monadBaseControl<S, F, F>(MonadBaseControl.id(BR())), release, use)
}

@extension
@undocumented
interface StateTMonadDefer<S, F> : MonadDefer<StateTPartialOf<S, F>>, StateTBracket<S, F, Throwable> {

  fun MD(): MonadDefer<F>
  override fun BR(): Bracket<F, Throwable> = MD()

  override fun <A> defer(fa: () -> StateTOf<S, F, A>): StateT<S, F, A> = MD().run {
    StateT { s -> defer { fa().run(s) } }
  }
}

@extension
@undocumented
interface StateTAsyncInstane<S, F> : Async<StateTPartialOf<S, F>>, StateTMonadDefer<S, F> {

  fun AS(): Async<F>

  override fun MD(): MonadDefer<F> = AS()

  override fun <A> async(fa: Proc<A>): StateT<S, F, A> = AS().run {
    StateT.liftF(this, async(fa))
  }

  override fun <A> asyncF(k: ProcF<StateTPartialOf<S, F>, A>): StateT<S, F, A> = AS().run {
    StateT { s ->
      asyncF<A> { cb -> k(cb).fix().runA(this, s) }
        .map { Tuple2(s, it) }
    }
  }

  override fun <A> StateTOf<S, F, A>.continueOn(ctx: CoroutineContext): StateT<S, F, A> = AS().run {
    StateT(AndThen(fix().runF).andThen { it.continueOn(ctx) })
  }
}

@extension
interface StateTMonadIO<S, F> : MonadIO<StateTPartialOf<S, F>>, StateTMonad<S, F> {
  fun FIO(): MonadIO<F>
  override fun MF(): Monad<F> = FIO()
  override fun <A> IO<A>.liftIO(): Kind<StateTPartialOf<S, F>, A> = FIO().run {
    StateT.liftF(this, liftIO())
  }
}
