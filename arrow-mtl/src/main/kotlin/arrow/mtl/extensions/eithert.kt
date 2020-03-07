package arrow.mtl.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.Eval
import arrow.core.Left
import arrow.core.Tuple2
import arrow.core.ap
import arrow.core.extensions.either.eq.eq
import arrow.core.extensions.either.foldable.foldable
import arrow.core.extensions.either.traverse.traverse
import arrow.core.fix
import arrow.core.identity
import arrow.core.left
import arrow.core.right
import arrow.core.toT
import arrow.extension
import arrow.mtl.EitherT
import arrow.mtl.EitherTOf
import arrow.mtl.EitherTPartialOf
import arrow.mtl.ForEitherT
import arrow.mtl.extensions.eithert.monadThrow.monadThrow
import arrow.mtl.fix
import arrow.mtl.typeclasses.ComposedTraverse
import arrow.mtl.typeclasses.MonadTrans
import arrow.mtl.typeclasses.Nested
import arrow.mtl.typeclasses.compose
import arrow.mtl.typeclasses.unnest
import arrow.mtl.value
import arrow.typeclasses.Alternative
import arrow.typeclasses.Applicative
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.Apply
import arrow.typeclasses.Contravariant
import arrow.typeclasses.Decidable
import arrow.typeclasses.Divide
import arrow.typeclasses.Divisible
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Foldable
import arrow.typeclasses.Functor
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.typeclasses.MonadThrow
import arrow.typeclasses.MonadThrowSyntax
import arrow.typeclasses.Monoid
import arrow.typeclasses.SemigroupK
import arrow.typeclasses.Traverse
import arrow.undocumented

@extension
@undocumented
interface EitherTFunctor<L, F> : Functor<EitherTPartialOf<L, F>> {

  fun FF(): Functor<F>

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(FF(), f)
}

@extension
@undocumented
interface EitherTApply<L, F> : Apply<EitherTPartialOf<L, F>>, EitherTFunctor<L, F> {

  fun AF(): Applicative<F>

  override fun FF(): Functor<F> = AF()

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(AF(), f)

  override fun <A, B> EitherTOf<L, F, A>.ap(ff: EitherTOf<L, F, (A) -> B>): EitherT<L, F, B> =
    fix().ap(AF(), ff)

  override fun <A, B> Kind<EitherTPartialOf<L, F>, A>.apEval(ff: Eval<Kind<EitherTPartialOf<L, F>, (A) -> B>>): Eval<Kind<EitherTPartialOf<L, F>, B>> =
    AF().run { value().apEval(ff.map { it.value().map { eitherF -> { eitherA: Either<L, A> -> eitherA.ap(eitherF) } } }) }
      .map(::EitherT)
}

@extension
@undocumented
interface EitherTApplicative<L, F> : Applicative<EitherTPartialOf<L, F>>, EitherTApply<L, F> {

  override fun AF(): Applicative<F>

  override fun FF(): Functor<F> = AF()

  override fun <A> just(a: A): EitherT<L, F, A> =
    EitherT.just(AF(), a)

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(AF(), f)
}

@extension
@undocumented
interface EitherTMonad<L, F> : Monad<EitherTPartialOf<L, F>>, EitherTApplicative<L, F> {

  fun MF(): Monad<F>

  override fun AF(): Applicative<F> = MF()

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(MF(), f)

  override fun <A, B> EitherTOf<L, F, A>.ap(ff: EitherTOf<L, F, (A) -> B>): EitherT<L, F, B> =
    fix().ap(MF(), ff)

  override fun <A, B> EitherTOf<L, F, A>.flatMap(f: (A) -> EitherTOf<L, F, B>): EitherT<L, F, B> =
    fix().flatMap(MF(), f)

  override fun <A, B> tailRecM(a: A, f: (A) -> EitherTOf<L, F, Either<A, B>>): EitherT<L, F, B> =
    EitherT.tailRecM(MF(), a, f)
}

@extension
@undocumented
interface EitherTApplicativeError<L, F, E> : ApplicativeError<EitherTPartialOf<L, F>, E>, EitherTApplicative<L, F> {

  fun AE(): ApplicativeError<F, E>

  override fun AF(): Applicative<F> = AE()

  override fun <A> raiseError(e: E): EitherT<L, F, A> =
    EitherT.liftF(AE(), AE().raiseError(e))

  override fun <A> EitherTOf<L, F, A>.handleErrorWith(f: (E) -> EitherTOf<L, F, A>): EitherT<L, F, A> = AE().run {
    EitherT(value().handleErrorWith { l -> f(l).value() })
  }
}

@extension
@undocumented
interface EitherTMonadError<L, F, E> : MonadError<EitherTPartialOf<L, F>, E>, EitherTApplicativeError<L, F, E>, EitherTMonad<L, F> {
  override fun MF(): Monad<F>
  override fun AE(): ApplicativeError<F, E>
  override fun AF(): Applicative<F> = MF()
}

fun <L, F, E> EitherT.Companion.monadError(ME: MonadError<F, E>): MonadError<EitherTPartialOf<L, F>, E> =
  object : EitherTMonadError<L, F, E> {
    override fun MF(): Monad<F> = ME
    override fun AE(): ApplicativeError<F, E> = ME
  }

@extension
@undocumented
interface EitherTMonadThrow<L, F> : MonadThrow<EitherTPartialOf<L, F>>, EitherTMonadError<L, F, Throwable> {
  override fun MF(): Monad<F>
  override fun AE(): ApplicativeError<F, Throwable>
}

@extension
@undocumented
interface EitherTFoldable<L, F> : Foldable<EitherTPartialOf<L, F>> {

  fun FFF(): Foldable<F>

  override fun <B, C> EitherTOf<L, F, B>.foldLeft(b: C, f: (C, B) -> C): C =
    fix().foldLeft(FFF(), b, f)

  override fun <B, C> EitherTOf<L, F, B>.foldRight(lb: Eval<C>, f: (B, Eval<C>) -> Eval<C>): Eval<C> =
    fix().foldRight(FFF(), lb, f)
}

@extension
@undocumented
interface EitherTTraverse<L, F> : Traverse<EitherTPartialOf<L, F>>, EitherTFunctor<L, F>, EitherTFoldable<L, F> {

  fun TF(): Traverse<F>

  override fun FF(): Functor<F> = TF()

  override fun FFF(): Foldable<F> = TF()

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(TF(), f)

  override fun <G, B, C> EitherTOf<L, F, B>.traverse(AP: Applicative<G>, f: (B) -> Kind<G, C>): Kind<G, EitherT<L, F, C>> =
    fix().traverse(TF(), AP, f)
}

@extension
@undocumented
interface EitherTSemigroupK<L, F> : SemigroupK<EitherTPartialOf<L, F>> {
  fun MF(): Monad<F>

  override fun <A> EitherTOf<L, F, A>.combineK(y: EitherTOf<L, F, A>): EitherT<L, F, A> =
    fix().combineK(MF(), y)
}

@extension
@undocumented
interface EitherTContravariant<L, F> : Contravariant<EitherTPartialOf<L, F>> {
  fun CF(): Contravariant<F>

  override fun <A, B> Kind<EitherTPartialOf<L, F>, A>.contramap(f: (B) -> A): Kind<EitherTPartialOf<L, F>, B> =
    EitherT(
      CF().run { value().contramap<Either<L, A>, Either<L, B>> { it.map(f) } }
    )
}

@extension
@undocumented
interface EitherTDivide<L, F> : Divide<EitherTPartialOf<L, F>>, EitherTContravariant<L, F> {
  fun DF(): Divide<F>
  override fun CF(): Contravariant<F> = DF()

  override fun <A, B, Z> divide(fa: Kind<EitherTPartialOf<L, F>, A>, fb: Kind<EitherTPartialOf<L, F>, B>, f: (Z) -> Tuple2<A, B>): Kind<EitherTPartialOf<L, F>, Z> =
    EitherT(
      DF().divide(fa.value(), fb.value()) { either ->
        either.fold({ it.left() toT it.left() }, {
          val (a, b) = f(it)
          a.right() toT b.right()
        })
      }
    )
}

@extension
@undocumented
interface EitherTDivisibleInstance<L, F> : Divisible<EitherTPartialOf<L, F>>, EitherTDivide<L, F> {

  fun DFF(): Divisible<F>
  override fun DF(): Divide<F> = DFF()

  override fun <A> conquer(): Kind<EitherTPartialOf<L, F>, A> =
    EitherT(
      DFF().conquer()
    )
}

@extension
@undocumented
interface EitherTDecidableInstance<L, F> : Decidable<EitherTPartialOf<L, F>>, EitherTDivisibleInstance<L, F> {

  fun DFFF(): Decidable<F>
  override fun DFF(): Divisible<F> = DFFF()

  override fun <A, B, Z> choose(fa: Kind<EitherTPartialOf<L, F>, A>, fb: Kind<EitherTPartialOf<L, F>, B>, f: (Z) -> Either<A, B>): Kind<EitherTPartialOf<L, F>, Z> =
    EitherT(
      DFFF().choose(fa.value(), fb.value()) { either ->
        either.map(f).fold({ left ->
          left.left().left()
        }, { e ->
          e.fold({ a ->
            a.right().left()
          }, { b ->
            b.right().right()
          })
        })
      }
    )
}

@extension
interface EitherTAlternative<L, F> : Alternative<EitherTPartialOf<L, F>>, EitherTApplicative<L, F> {
  override fun AF(): Applicative<F> = MF()
  fun MF(): Monad<F>
  fun ME(): Monoid<L>

  override fun <A> empty(): Kind<EitherTPartialOf<L, F>, A> = EitherT(MF().just(ME().empty().left()))

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.orElse(b: Kind<EitherTPartialOf<L, F>, A>): Kind<EitherTPartialOf<L, F>, A> =
    EitherT(
      MF().fx.monad {
        val l = !value()
        l.fold({ ll ->
          val r = !b.value()
          r.fold({
            ME().run { (ll + it).left() }
          }, {
            it.right()
          })
        }, {
          it.right()
        })
      }
    )
}

fun <A, F, B, C> EitherTOf<A, F, B>.foldLeft(FF: Foldable<F>, b: C, f: (C, B) -> C): C =
  FF.compose(Either.foldable<A>()).foldLC(value(), b, f)

fun <A, F, B, C> EitherTOf<A, F, B>.foldRight(FF: Foldable<F>, lb: Eval<C>, f: (B, Eval<C>) -> Eval<C>): Eval<C> = FF.compose(Either.foldable<A>()).run {
  value().foldRC(lb, f)
}

fun <A, F, B, G, C> EitherTOf<A, F, B>.traverse(FF: Traverse<F>, GA: Applicative<G>, f: (B) -> Kind<G, C>): Kind<G, EitherT<A, F, C>> {
  val fa: Kind<G, Kind<Nested<F, EitherPartialOf<A>>, C>> = ComposedTraverse(FF, Either.traverse<A>()).run { value().traverseC(f, GA) }
  val mapper: (Kind<Nested<F, EitherPartialOf<A>>, C>) -> EitherT<A, F, C> = { nested -> EitherT(FF.run { nested.unnest().map { it.fix() } }) }
  return GA.run { fa.map(mapper) }
}

fun <A, G, F, B> EitherTOf<A, F, Kind<G, B>>.sequence(FF: Traverse<F>, GA: Applicative<G>): Kind<G, EitherT<A, F, B>> =
  traverse(FF, GA, ::identity)

fun <L, F> EitherT.Companion.applicativeError(MF: Monad<F>): ApplicativeError<EitherTPartialOf<L, F>, L> =
  object : ApplicativeError<EitherTPartialOf<L, F>, L>, EitherTApplicative<L, F> {

    override fun AF(): Applicative<F> = MF

    override fun <A> raiseError(e: L): EitherTOf<L, F, A> =
      EitherT(MF.just(Left(e)))

    override fun <A> EitherTOf<L, F, A>.handleErrorWith(f: (L) -> EitherTOf<L, F, A>): EitherT<L, F, A> =
      handleErrorWith(this, f, MF)
  }

fun <L, F> EitherT.Companion.monadError(MF: Monad<F>): MonadError<EitherTPartialOf<L, F>, L> =
  object : MonadError<EitherTPartialOf<L, F>, L>, EitherTMonad<L, F> {
    override fun MF(): Monad<F> = MF

    override fun <A> raiseError(e: L): EitherTOf<L, F, A> =
      EitherT(MF.just(Left(e)))

    override fun <A> EitherTOf<L, F, A>.handleErrorWith(f: (L) -> EitherTOf<L, F, A>): EitherT<L, F, A> =
      handleErrorWith(this, f, MF())
  }

private fun <L, F, A> handleErrorWith(fa: EitherTOf<L, F, A>, f: (L) -> EitherTOf<L, F, A>, MF: Monad<F>): EitherT<L, F, A> =
  MF.run {
    EitherT(fa.value().flatMap {
      when (it) {
        is Either.Left -> f(it.a).value()
        is Either.Right -> just(it)
      }
    })
  }

fun <L, F, R> EitherT.Companion.fx(M: MonadThrow<F>, c: suspend MonadThrowSyntax<EitherTPartialOf<L, F>>.() -> R): EitherT<L, F, R> =
  EitherT.monadThrow<L, F>(M, M).fx.monadThrow(c).fix()

@extension
interface EitherTEqK<L, F> : EqK<EitherTPartialOf<L, F>> {
  fun EQKF(): EqK<F>

  fun EQL(): Eq<L>

  override fun <R> Kind<EitherTPartialOf<L, F>, R>.eqK(other: Kind<EitherTPartialOf<L, F>, R>, EQ: Eq<R>): Boolean =
    (this.fix() to other.fix()).let {
      EQKF().liftEq(Either.eq(EQL(), EQ)).run {
        it.first.value().eqv(it.second.value())
      }
    }
}

@extension
interface EitherTMonadTrans<L> : MonadTrans<Kind<ForEitherT, L>> {
  override fun <G, A> Kind<G, A>.liftT(MG: Monad<G>): Kind2<Kind<ForEitherT, L>, G, A> =
    EitherT.liftF(MG, this)
}
