package arrow.mtl.typeclasses

import arrow.Kind
import arrow.Kind2
import arrow.typeclasses.Monad

/**
 * [MonadTransControl] extends [MonadTrans] with the ability to save and restore the monadic state added by the transformer.
 *
 * This allows to lift functions that have a transformer in both the negative and positive argument position:
 * ```kotlin
 * fun withFile(f: File -> IO<A>): IO<A> = TODO()
 * ```
 * If we now want to define:
 * ```kotlin
 * fun withFile(f: File -> Kind<T, Kind<ForIO, A>>): Kind<T, Kind<ForIO, A>> = ???
 *
 * // concrete example with WriterT
 * fun withFile(f: File -> WriterT<Log, ForIO, A>): WriterT<Log, ForIO, A> = ???
 * ```
 * This is a bit troublesome because we cannot use only `MonadTrans` with `liftT` to implement this. This is where `MonadTransControl` comes in:
 * ```kotlin
 * fun withFile(f: File -> Kind<T, Kind<ForIO, A>>): Kind<T, Kind<ForIO, A>> =
 *   liftWith { runT -> withFile { file -> runT(f(file)) } }.flatMap { just(it).restoreT() }
 * ```
 * In the above example the `runT` function strips off the monadic state from the result of `f`. This state is later used with `restoreT` to restore the lost state.
 */
interface MonadTransControl<T> : MonadTrans<T> {

  fun <M, A> liftWith(MM: Monad<M>, f: (RunT<T>) -> Kind<M, A>): Kind<Kind<T, M>, A>

  fun <M, A> Kind<M, StT<T, A>>.restoreT(MM: Monad<M>): Kind<Kind<T, M>, A>

  override fun <G, A> Kind<G, A>.liftT(MG: Monad<G>): Kind2<T, G, A> = liftWith(MG) { this@liftT }
}

interface RunT<T> {
  operator fun <M, A> invoke(MM: Monad<M>, fa: Kind<Kind<T, M>, A>): Kind<M, StT<T, A>>
}

// TODO Can this be replaced to work entirely at the typelevel with arrow-meta?
class StT<T, A>(val unsafeState: Any?)
