package arrow.fx.mtl

import arrow.Kind
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.flatMap
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
import arrow.mtl.EitherT
import arrow.mtl.EitherTOf
import arrow.mtl.EitherTPartialOf
import arrow.mtl.extensions.EitherTMonad
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.fix
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.mtl.value
import arrow.typeclasses.Monad
import arrow.undocumented
import kotlin.coroutines.CoroutineContext

@extension
@undocumented
interface EitherTBracket<L, F, E> : Bracket<EitherTPartialOf<L, F>, E>, EitherTMonad<L, F> {

  fun BR(): Bracket<F, E>

  override fun MF(): Monad<F> = BR()

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.handleErrorWith(f: (E) -> Kind<EitherTPartialOf<L, F>, A>): Kind<EitherTPartialOf<L, F>, A> =
    EitherT(BR().run { fix().value().handleErrorWith { e -> f(e).value() } })

  override fun <A> raiseError(e: E): Kind<EitherTPartialOf<L, F>, A> =
    EitherT.liftF(BR(), BR().raiseError(e))

  override fun <A, B> Kind<EitherTPartialOf<L, F>, A>.bracketCase(release: (A, ExitCase<E>) -> Kind<EitherTPartialOf<L, F>, Unit>, use: (A) -> Kind<EitherTPartialOf<L, F>, B>): Kind<EitherTPartialOf<L, F>, B> =
    defaultBracket(BR(), EitherT.monadBaseControl<L, F, F>(MonadBaseControl.id(BR())), release, use)
}

@extension
@undocumented
interface EitherTMonadDefer<L, F> : MonadDefer<EitherTPartialOf<L, F>>, EitherTBracket<L, F, Throwable> {

  fun MDF(): MonadDefer<F>
  override fun BR(): Bracket<F, Throwable> = MDF()

  override fun <A> defer(fa: () -> EitherTOf<L, F, A>): EitherT<L, F, A> =
    EitherT(MDF().defer { fa().value() })
}

@extension
@undocumented
interface EitherTAsync<L, F> : Async<EitherTPartialOf<L, F>>, EitherTMonadDefer<L, F> {

  fun ASF(): Async<F>

  override fun MDF(): MonadDefer<F> = ASF()

  override fun <A> async(fa: Proc<A>): EitherT<L, F, A> = ASF().run {
    EitherT.liftF(this, async(fa))
  }

  override fun <A> asyncF(k: ProcF<EitherTPartialOf<L, F>, A>): EitherT<L, F, A> = ASF().run {
    EitherT.liftF(this, asyncF { cb -> k(cb).value().unit() })
  }

  override fun <A> EitherTOf<L, F, A>.continueOn(ctx: CoroutineContext): EitherT<L, F, A> = ASF().run {
    EitherT(value().continueOn(ctx))
  }
}

interface EitherTConcurrent<L, F> : Concurrent<EitherTPartialOf<L, F>>, EitherTAsync<L, F> {

  fun CF(): Concurrent<F>

  override fun ASF(): Async<F> = CF()

  override fun dispatchers(): Dispatchers<EitherTPartialOf<L, F>> =
    CF().dispatchers() as Dispatchers<EitherTPartialOf<L, F>>

  override fun <A> cancelable(k: ((Either<Throwable, A>) -> Unit) -> CancelToken<EitherTPartialOf<L, F>>): EitherT<L, F, A> = CF().run {
    EitherT.liftF(this, cancelable { cb -> k(cb).value().map { Unit } })
  }

  override fun <A> EitherTOf<L, F, A>.fork(ctx: CoroutineContext): EitherT<L, F, Fiber<EitherTPartialOf<L, F>, A>> = CF().run {
    EitherT.liftF(this, value().fork(ctx).map(::fiberT))
  }

  override fun <A, B> parTupledN(ctx: CoroutineContext, fa: EitherTOf<L, F, A>, fb: EitherTOf<L, F, B>): Kind<EitherTPartialOf<L, F>, Tuple2<A, B>> = CF().run {
    EitherT(parMapN(ctx, fa.value(), fb.value()) { (a, b) ->
      a.flatMap { aa ->
        b.map { bb -> Tuple2(aa, bb) }
      }
    })
  }

  override fun <A, B, C> parTupledN(ctx: CoroutineContext, fa: EitherTOf<L, F, A>, fb: EitherTOf<L, F, B>, fc: EitherTOf<L, F, C>): EitherT<L, F, Tuple3<A, B, C>> = CF().run {
    EitherT(parMapN(ctx, fa.value(), fb.value(), fc.value()) { (a, b, c) ->
      a.flatMap { aa ->
        b.flatMap { bb ->
          c.map { cc ->
            Tuple3(aa, bb, cc)
          }
        }
      }
    })
  }

  override fun <A, B> CoroutineContext.racePair(fa: EitherTOf<L, F, A>, fb: EitherTOf<L, F, B>): EitherT<L, F, RacePair<EitherTPartialOf<L, F>, A, B>> = CF().run {
    val racePair: Kind<F, Either<L, RacePair<EitherTPartialOf<L, F>, A, B>>> =
      racePair(fa.value(), fb.value()).flatMap { res: RacePair<F, Either<L, A>, Either<L, B>> ->
        when (res) {
          is RacePair.First -> when (val winner = res.winner) {
            is Either.Left -> res.fiberB.cancel().map { Left(winner.a) }
            is Either.Right -> just(Right(RacePair.First(winner.b, fiberT(res.fiberB))))
          }
          is RacePair.Second -> when (val winner = res.winner) {
            is Either.Left -> res.fiberA.cancel().map { Left(winner.a) }
            is Either.Right -> just(Right(RacePair.Second(fiberT(res.fiberA), winner.b)))
          }
        }
      }
    EitherT(racePair)
  }

  override fun <A, B, C> CoroutineContext.raceTriple(
    fa: EitherTOf<L, F, A>,
    fb: EitherTOf<L, F, B>,
    fc: EitherTOf<L, F, C>
  ): EitherT<L, F, RaceTriple<EitherTPartialOf<L, F>, A, B, C>> = CF().run {
    val raceTriple: Kind<F, Either<L, RaceTriple<EitherTPartialOf<L, F>, A, B, C>>> =
      raceTriple(fa.value(), fb.value(), fc.value()).flatMap { res: RaceTriple<F, Either<L, A>, Either<L, B>, Either<L, C>> ->
        when (res) {
          is RaceTriple.First -> when (val winner = res.winner) {
            is Either.Left -> tupledN(res.fiberB.cancel(), res.fiberC.cancel()).map { Left(winner.a) }
            is Either.Right -> just(Right(RaceTriple.First(winner.b, fiberT(res.fiberB), fiberT(res.fiberC))))
          }
          is RaceTriple.Second -> when (val winner = res.winner) {
            is Either.Left -> tupledN(res.fiberA.cancel(), res.fiberC.cancel()).map { Left(winner.a) }
            is Either.Right -> just(Right(RaceTriple.Second(fiberT(res.fiberA), winner.b, fiberT(res.fiberC))))
          }
          is RaceTriple.Third -> when (val winner = res.winner) {
            is Either.Left -> tupledN(res.fiberA.cancel(), res.fiberB.cancel()).map { Left(winner.a) }
            is Either.Right -> just(Right(RaceTriple.Third(fiberT(res.fiberA), fiberT(res.fiberB), winner.b)))
          }
        }
      }
    EitherT(raceTriple)
  }

  fun <A> fiberT(fiber: Fiber<F, Either<L, A>>): Fiber<EitherTPartialOf<L, F>, A> =
    Fiber(EitherT(fiber.join()), EitherT.liftF(ASF(), fiber.cancel()))
}

fun <L, F> EitherT.Companion.concurrent(CF: Concurrent<F>): Concurrent<EitherTPartialOf<L, F>> =
  object : EitherTConcurrent<L, F> {
    override fun CF(): Concurrent<F> = CF
  }

fun <L, F> EitherT.Companion.timer(CF: Concurrent<F>): Timer<EitherTPartialOf<L, F>> =
  Timer(concurrent<L, F>(CF))

@extension
interface EitherTMonadIO<L, F> : MonadIO<EitherTPartialOf<L, F>>, EitherTMonad<L, F> {
  fun FIO(): MonadIO<F>
  override fun MF(): Monad<F> = FIO()
  override fun <A> IO<A>.liftIO(): Kind<EitherTPartialOf<L, F>, A> = FIO().run {
    EitherT.liftF(this, liftIO())
  }
}
