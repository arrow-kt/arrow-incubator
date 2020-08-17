package arrow.mtl.typeclasses

import arrow.Kind
import arrow.typeclasses.Monad

/**
 * [MonadBaseControl] provides means of removing and restoring the monadic state with a specified base monad
 *
 * This basically allows for the same function as [MonadTransControl] but allows specifying a fixed base monad:
 * ```kotlin
 * fun withFile(f: File -> IO<A>): IO<A> = TODO()
 * ```
 * Similar to the [MonadTransControl] example we want to lift this as well:
 * ```kotlin
 * fun withFileM(f: (File) -> Kind<M, A>): Kind<M, A> = ???
 * ```
 * The difference to the MonadTransControl code is that here we do not need to leave the specific base monad in the type
 * This provides the same benefit that MonadIO does for "unlifting", it leaves the monad stack abstract, while keeping evidence of IO as the base.
 *
 * An implementation might look like this:
 * ```kotlin
 * fun <M> withFileM(MBC: MonadBaseControl<ForIO, M>, f: (File) -> Kind<M, A>): Kind<M, A> =
 *   MBC.MM().run { // get the monad instance for M in scope
 *     MBC.liftBaseWith { runInBase -> withFile { file -> runInBase(f(file)) } }
 *       .flatMap { ctx -> MBC.run { ctx.restoreM() } }
 *   }
 *
 * // Because using liftBaseWith and flatMap { it.restoreM } together is a very common method there is a utility function called control:
 * fun <M> withFileM(MBC: MonadBaseControl<ForIO, M>, f: (File) -> Kind<M, A>): Kind<M, A> =
 *   MBC.control { runInBase -> withFile { file -> runInBase(f(file)) } }
 * ```
 *
 * This approach has limitations, mainly that it fails in a non-polymorphic setting.
 * For example it is easy to lift `fun <A> g(a: IO<A>): IO<A>` because it is polymorphic. It is however impossible to lift `fun h(a: IO<Any>): IO<Any>` without discarding the monadic state of the action.
 * [MonadBaseControl] relies on polymorphism to retrieve the monadic state from the action, if the action is not polymorphic that state has to be discarded.
 * Even if the action is polymorphic not everything may be recovered, functions like `fun <A, B> g(a: IO<A>, b: IO<B>): IO<A>` also have to discard state when lifted.
 * However even with these problems it is a useful and working method of integrating functions that take concrete monadic actions as arguments into arbitrary monad stacks.
 *
 * ank_macro_hierarchy(arrow.mtl.typeclasses.MonadBaseControl)
 */
interface MonadBaseControl<B, M> : MonadBase<B, M> {

  /**
   * Provide a function with can remove all monadic state from any action [M], can be used in combination with [restoreM] to remove and restore monadic context over certain boundaries.
   *
   * This returns `Kind<M, A>` because to unlift actions of type [M] it usually needs access to some monadic state.
   * For example unlifting [Kleisli] first needs to retrieve the stored context.
   */
  fun <A> liftBaseWith(f: (RunInBase<M, B>) -> Kind<B, A>): Kind<M, A>

  /**
   * Restores the monadic context from the concrete saved monadic state inside [StM].
   */
  fun <A> StM<M, A>.restoreM(): Kind<M, A>

  /**
   * Shorthand for `MBC.liftBaseWith {}.flatMap { it.restoreM() }`
   */
  fun <A> control(f: (RunInBase<M, B>) -> Kind<B, StM<M, A>>): Kind<M, A> =
    MM().run { liftBaseWith(f).flatMap { it.restoreM() } }

  override fun <A> Kind<B, A>.liftBase(): Kind<M, A> = liftBaseWith { this@liftBase }

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
        override fun MM(): Monad<Kind<T, M>> = MTC.liftMonad(MBC.MM())
        override fun <A> liftBaseWith(f: (RunInBase<Kind<T, M>, B>) -> Kind<B, A>): Kind<Kind<T, M>, A> = MTC.liftWith(MBC.MM()) { runT ->
          MBC.liftBaseWith { runMB ->
            f(object : RunInBase<Kind<T, M>, B> {
              override fun <A> invoke(fa: Kind<Kind<T, M>, A>): Kind<B, StM<Kind<T, M>, A>> =
                MB().run { runMB(runT(MBC.MM(), fa)).map { StM<Kind<T, M>, A>(it) } }
            })
          }
        }
        override fun <A> StM<Kind<T, M>, A>.restoreM(): Kind<Kind<T, M>, A> =
          MTC.run { MBC.run { (this@restoreM.unsafeState as StM<M, StT<T, A>>).restoreM() }.restoreT(MBC.MM()) }
      }
  }
}

interface RunInBase<M, B> {
  operator fun <A> invoke(fa: Kind<M, A>): Kind<B, StM<M, A>>
}

class StM<M, A>(val unsafeState: Any?)
