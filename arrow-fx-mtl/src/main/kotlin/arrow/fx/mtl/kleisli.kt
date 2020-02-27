package arrow.fx.mtl

import arrow.Kind
import arrow.core.AndThen
import arrow.core.Either
import arrow.extension
import arrow.fx.IO
import arrow.fx.RacePair
import arrow.fx.RaceTriple
import arrow.fx.Timer
import arrow.fx.mtl.unlifted.defaultBracket
import arrow.fx.typeclasses.Async
import arrow.fx.typeclasses.Bracket
import arrow.fx.typeclasses.CancelToken
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.Dispatchers
import arrow.fx.typeclasses.ExitCase
import arrow.fx.typeclasses.Fiber
import arrow.fx.typeclasses.MonadDefer
import arrow.fx.typeclasses.MonadIO
import arrow.fx.typeclasses.Proc
import arrow.fx.typeclasses.ProcF
import arrow.mtl.Kleisli
import arrow.mtl.KleisliOf
import arrow.mtl.KleisliPartialOf
import arrow.mtl.extensions.KleisliMonad
import arrow.mtl.extensions.KleisliMonadError
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
interface KleisliBracket<D, F, E> : Bracket<KleisliPartialOf<D, F>, E>, KleisliMonadError<D, F, E> {

  fun BF(): Bracket<F, E>

  override fun ME(): MonadError<F, E> = BF()

  override fun <A, B> Kind<KleisliPartialOf<D, F>, A>.bracketCase(release: (A, ExitCase<E>) -> Kind<KleisliPartialOf<D, F>, Unit>, use: (A) -> Kind<KleisliPartialOf<D, F>, B>): Kind<KleisliPartialOf<D, F>, B> =
    defaultBracket(BF(), Kleisli.monadBaseControl<D, F, F>(MonadBaseControl.id(BF())), release, use)

  override fun <A> KleisliOf<D, F, A>.uncancelable(): Kleisli<D, F, A> = BF().run {
    Kleisli { r -> this@uncancelable.run(r).uncancelable() }
  }
}

@extension
@undocumented
interface KleisliMonadDefer<D, F> : MonadDefer<KleisliPartialOf<D, F>>, KleisliBracket<D, F, Throwable> {

  fun MDF(): MonadDefer<F>

  override fun BF(): Bracket<F, Throwable> = MDF()

  override fun <A> defer(fa: () -> KleisliOf<D, F, A>): Kleisli<D, F, A> = MDF().run {
    Kleisli { r -> defer { fa().run(r) } }
  }

  override fun <A> KleisliOf<D, F, A>.handleErrorWith(f: (Throwable) -> KleisliOf<D, F, A>): Kleisli<D, F, A> = MDF().run {
    Kleisli { d -> defer { run(d).handleErrorWith { e -> f(e).run(d) } } }
  }

  override fun <A, B> KleisliOf<D, F, A>.flatMap(f: (A) -> KleisliOf<D, F, B>): Kleisli<D, F, B> = MDF().run {
    Kleisli { d -> defer { run(d).flatMap { a -> f(a).run(d) } } }
  }

  override fun <A> KleisliOf<D, F, A>.uncancelable(): Kleisli<D, F, A> = MDF().run {
    Kleisli { d -> defer { run(d).uncancelable() } }
  }
}

fun <D, F> Kleisli.Companion.monadDefer(MD: MonadDefer<F>): MonadDefer<KleisliPartialOf<D, F>> =
  object : KleisliMonadDefer<D, F> {
    override fun MDF(): MonadDefer<F> = MD
  }

@extension
@undocumented
interface KleisliAsync<D, F> : Async<KleisliPartialOf<D, F>>, KleisliMonadDefer<D, F> {

  fun ASF(): Async<F>

  override fun MDF(): MonadDefer<F> = ASF()

  override fun <A> async(fa: Proc<A>): Kleisli<D, F, A> =
    Kleisli.liftF(ASF().async(fa))

  override fun <A> asyncF(k: ProcF<KleisliPartialOf<D, F>, A>): Kleisli<D, F, A> =
    Kleisli { r -> ASF().asyncF { cb -> k(cb).run(r) } }

  override fun <A> KleisliOf<D, F, A>.continueOn(ctx: CoroutineContext): Kleisli<D, F, A> = ASF().run {
    Kleisli(AndThen(fix().run).andThen { it.continueOn(ctx) })
  }
}

fun <D, F> Kleisli.Companion.async(AS: Async<F>): Async<KleisliPartialOf<D, F>> =
  object : KleisliAsync<D, F> {
    override fun ASF(): Async<F> = AS
  }

interface KleisliConcurrent<D, F> : Concurrent<KleisliPartialOf<D, F>>, KleisliAsync<D, F> {

  fun CF(): Concurrent<F>
  override fun ASF(): Async<F> = CF()

  override fun dispatchers(): Dispatchers<KleisliPartialOf<D, F>> =
    CF().dispatchers() as Dispatchers<KleisliPartialOf<D, F>>

  override fun <A> cancelable(k: ((Either<Throwable, A>) -> Unit) -> CancelToken<KleisliPartialOf<D, F>>): Kleisli<D, F, A> = CF().run {
    Kleisli { d -> cancelable { cb -> k(cb).run(d).map { Unit } } }
  }

  override fun <A> KleisliOf<D, F, A>.fork(ctx: CoroutineContext): Kleisli<D, F, Fiber<KleisliPartialOf<D, F>, A>> = CF().run {
    Kleisli { r -> run(r).fork(ctx).map(::fiberT) }
  }

  override fun <A, B, C> CoroutineContext.parMapN(fa: KleisliOf<D, F, A>, fb: KleisliOf<D, F, B>, f: (A, B) -> C): Kleisli<D, F, C> = CF().run {
    Kleisli { r ->
      just(r).flatMap { rr ->
        parMapN(fa.run(rr), fb.run(rr), f)
      }
    }
  }

  override fun <A, B, C, DD> CoroutineContext.parMapN(fa: KleisliOf<D, F, A>, fb: KleisliOf<D, F, B>, fc: KleisliOf<D, F, C>, f: (A, B, C) -> DD): Kleisli<D, F, DD> = CF().run {
    Kleisli { r ->
      just(r).flatMap { rr ->
        parMapN(fa.run(rr), fb.run(rr), fc.run(rr), f)
      }
    }
  }

  override fun <A, B> CoroutineContext.racePair(fa: KleisliOf<D, F, A>, fb: KleisliOf<D, F, B>): Kleisli<D, F, RacePair<KleisliPartialOf<D, F>, A, B>> = CF().run {
    Kleisli { r ->
      just(r).flatMap { rr ->
        racePair(fa.run(rr), fb.run(rr)).map { res: RacePair<F, A, B> ->
          when (res) {
            is RacePair.First -> RacePair.First(res.winner, fiberT(res.fiberB))
            is RacePair.Second -> RacePair.Second(fiberT(res.fiberA), res.winner)
          }
        }
      }
    }
  }

  override fun <A, B, C> CoroutineContext.raceTriple(fa: KleisliOf<D, F, A>, fb: KleisliOf<D, F, B>, fc: KleisliOf<D, F, C>): Kleisli<D, F, RaceTriple<KleisliPartialOf<D, F>, A, B, C>> = CF().run {
    Kleisli { r ->
      just(r).flatMap { rr ->
        raceTriple(fa.run(rr), fb.run(rr), fc.run(rr)).map { res: RaceTriple<F, A, B, C> ->
          when (res) {
            is RaceTriple.First -> RaceTriple.First(res.winner, fiberT(res.fiberB), fiberT(res.fiberC))
            is RaceTriple.Second -> RaceTriple.Second(fiberT(res.fiberA), res.winner, fiberT(res.fiberC))
            is RaceTriple.Third -> RaceTriple.Third(fiberT(res.fiberA), fiberT(res.fiberB), res.winner)
          }
        }
      }
    }
  }

  fun <A> fiberT(fiber: Fiber<F, A>): Fiber<KleisliPartialOf<D, F>, A> =
    Fiber(Kleisli.liftF(fiber.join()), Kleisli.liftF(fiber.cancel()))
}

fun <D, F> Kleisli.Companion.concurrent(CF: Concurrent<F>): Concurrent<KleisliPartialOf<D, F>> =
  object : KleisliConcurrent<D, F> {
    override fun CF(): Concurrent<F> = CF
  }

fun <D, F> Kleisli.Companion.timer(CF: Concurrent<F>): Timer<KleisliPartialOf<D, F>> =
  Timer(concurrent<D, F>(CF))

@extension
interface KleisliMonadIO<D, F> : MonadIO<KleisliPartialOf<D, F>>, KleisliMonad<D, F> {
  fun FIO(): MonadIO<F>
  override fun MF(): Monad<F> = FIO()
  override fun <A> IO<A>.liftIO(): Kind<KleisliPartialOf<D, F>, A> = FIO().run {
    Kleisli.liftF(liftIO())
  }
}
