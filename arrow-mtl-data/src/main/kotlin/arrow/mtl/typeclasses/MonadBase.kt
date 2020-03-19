package arrow.mtl.typeclasses

import arrow.Kind
import arrow.typeclasses.Monad

/**
 * Lift a computation from `Kind<B, A>` to `Kind<M, A>`
 *
 * Useful if you want to specify an exact base monad for a polymorphic monad stack.
 *
 * For example:
 * - `MonadIO<M>` == `MonadBase<ForIO, M>`
 *
 * ank_macro_hierarchy(arrow.mtl.typeclasses.MonadBase)
 */
interface MonadBase<B, M> {
  fun MM(): Monad<M>
  fun MB(): Monad<B>

  fun <A> Kind<B, A>.liftBase(): Kind<M, A>

  companion object {
    fun <M> id(MM: Monad<M>): MonadBase<M, M> = object : MonadBase<M, M> {
      override fun MB(): Monad<M> = MM
      override fun MM(): Monad<M> = MM
      override fun <A> Kind<M, A>.liftBase(): Kind<M, A> = this
    }

    fun <T, B, M> defaultImpl(MT: MonadTrans<T>, MB: MonadBase<B, M>): MonadBase<B, Kind<T, M>> = object : MonadBase<B, Kind<T, M>> {
      override fun MB(): Monad<B> = MB.MB()
      override fun MM(): Monad<Kind<T, M>> = MT.liftMonad(MB.MM())

      override fun <A> Kind<B, A>.liftBase(): Kind<Kind<T, M>, A> =
        MB.run { MT.run { liftBase().liftT(MB.MM()) } }
    }
  }
}
