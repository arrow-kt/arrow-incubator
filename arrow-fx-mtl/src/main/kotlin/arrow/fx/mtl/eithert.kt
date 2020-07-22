package arrow.fx.mtl

import arrow.Kind
import arrow.core.Either
import arrow.core.Left
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.Some
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.andThen
import arrow.core.flatMap
import arrow.extension
import arrow.fx.IO
import arrow.fx.RacePair
import arrow.fx.RaceTriple
import arrow.fx.Ref
import arrow.fx.Timer
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
import arrow.mtl.extensions.EitherTMonadThrow
import arrow.mtl.value
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadThrow
import arrow.undocumented
import kotlin.coroutines.CoroutineContext

@extension
@undocumented
interface EitherTBracket<L, F> : Bracket<EitherTPartialOf<L, F>, Throwable>, EitherTMonadThrow<L, F> {

  fun MDF(): MonadDefer<F>
  override fun MT(): MonadThrow<F> = MDF()

  override fun <A, B> EitherTOf<L, F, A>.bracketCase(
    release: (A, ExitCase<Throwable>) -> EitherTOf<L, F, Unit>,
    use: (A) -> EitherTOf<L, F, B>
  ): EitherT<L, F, B> = MDF().run {
    EitherT.liftF<L, F, Ref<F, Option<L>>>(this, Ref(None)).flatMap { ref ->
      EitherT(value().bracketCase(use = { either ->
        when (either) {
          is Either.Right -> use(either.b).value()
          is Either.Left -> just(either)
        }
      }, release = { either, exitCase ->
        when (either) {
          is Either.Right -> when (exitCase) {
            is ExitCase.Completed -> release(either.b, ExitCase.Completed).value().flatMap {
              it.fold({ l ->
                ref.set(Some(l))
              }, {
                just(Unit)
              })
            }
            else -> release(either.b, exitCase).value().unit()
          }
          is Either.Left -> just(Unit)
        }
      }).flatMap { either ->
        when (either) {
          is Either.Right -> ref.get().map {
            it.fold({ either }, { left -> Left(left) })
          }
          is Either.Left -> just(either)
        }
      })
    }
  }
}

@extension
@undocumented
interface EitherTMonadDefer<L, F> : MonadDefer<EitherTPartialOf<L, F>>, EitherTBracket<L, F> {

  override fun MDF(): MonadDefer<F>

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

  override fun <A> cancellable(k: ((Either<Throwable, A>) -> Unit) -> CancelToken<EitherTPartialOf<L, F>>): EitherT<L, F, A> = CF().run {
    EitherT.liftF(this, cancellable(k.andThen { it.value().void() }))
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

interface EitherTMonadIO<L, F> : MonadIO<EitherTPartialOf<L, F>>, EitherTMonad<L, F> {
  fun FIO(): MonadIO<F>
  override fun MF(): Monad<F> = FIO()
  override fun <A> IO<A>.liftIO(): Kind<EitherTPartialOf<L, F>, A> = FIO().run {
    EitherT.liftF(this, liftIO())
  }
}
