package arrow.mtl.typeclasses

import arrow.Kind
import arrow.typeclasses.Monad

interface MonadBaseControl<B, M> : MonadBase<B, M> {

  fun <A> liftBaseWith(f: (RunInBase<M, B>) -> Kind<B, A>): Kind<M, A>

  fun <A> StM<M, A>.restoreM(): Kind<M, A>

  override fun <A> Kind<B, A>.liftBase(): Kind<M, A> = liftBaseWith { this@liftBase }

  fun <A> control(f: (RunInBase<M, B>) -> Kind<B, StM<M, A>>): Kind<M, A> =
    MM().run { liftBaseWith(f).flatMap { it.restoreM() } }

  companion object {
    fun <M> id(MM: Monad<M>): MonadBaseControl<M, M> = object : MonadBaseControl<M, M> {
      override fun MB(): Monad<M> = MM
      override fun MM(): Monad<M> = MM
      val runInBase = object : RunInBase<M, M> {
        override fun <A> invoke(fa: Kind<M, A>): Kind<M, StM<M, A>> = MM.run { fa.map { StM<M, A>(it) } }
      }
      override fun <A> liftBaseWith(f: (RunInBase<M, M>) -> Kind<M, A>): Kind<M, A> = f(runInBase)
      override fun <A> StM<M, A>.restoreM(): Kind<M, A> = MM.just(unsafeState as A)
    }

    /**
     * Default in terms of MonadTransControl
     */
    fun <T, M, B> defaultImpl(MTC: MonadTransControl<T>, MBC: MonadBaseControl<B, M>): MonadBaseControl<B, Kind<T, M>> =
      object : MonadBaseControl<B, Kind<T, M>> {
        override fun MB(): Monad<B> = MBC.MB()
        override fun MM(): Monad<Kind<T, M>> = MTC.monad(MBC.MM())
        override fun <A> liftBaseWith(f: (RunInBase<Kind<T, M>, B>) -> Kind<B, A>): Kind<Kind<T, M>, A> = MTC.liftWith(MBC.MM()) { runT ->
          MBC.liftBaseWith { runMB ->
            f(object : RunInBase<Kind<T, M>, B> {
              override fun <A> invoke(fa: Kind<Kind<T, M>, A>): Kind<B, StM<Kind<T, M>, A>> =
                runMB(runT(MBC.MM(), fa)) as Kind<B, StM<Kind<T, M>, A>>
            })
          }
        }
        override fun <A> StM<Kind<T, M>, A>.restoreM(): Kind<Kind<T, M>, A> =
          MTC.run { MBC.run { (this@restoreM as StM<M, StT<T, A>>).restoreM() }.restoreT(MBC.MM()) }
      }
  }
}

interface RunInBase<M, B> {
  operator fun <A> invoke(fa: Kind<M, A>): Kind<B, StM<M, A>>
}

class StM<M, A>(val unsafeState: Any?)
