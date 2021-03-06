package arrow.mtl.extensions

  import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.Eval
import arrow.core.Eval.Now
import arrow.core.Right
import arrow.core.Tuple2
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
import arrow.mtl.typeclasses.MonadReader
import arrow.mtl.typeclasses.MonadState
import arrow.mtl.typeclasses.MonadTrans
import arrow.mtl.typeclasses.MonadWriter
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

  fun MF(): Monad<F>

  override fun FF(): Functor<F> = MF()

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(MF(), f)

  override fun <A, B> EitherTOf<L, F, A>.ap(ff: EitherTOf<L, F, (A) -> B>): EitherT<L, F, B> =
    fix().ap(MF(), ff)

  override fun <A, B> Kind<EitherTPartialOf<L, F>, A>.apEval(ff: Eval<Kind<EitherTPartialOf<L, F>, (A) -> B>>): Eval<Kind<EitherTPartialOf<L, F>, B>> =
    fix().flatMap(MF()) { a -> ff.value().map { f -> f(a) } }.let(::Now)
}

@extension
@undocumented
interface EitherTApplicative<L, F> : Applicative<EitherTPartialOf<L, F>>, EitherTApply<L, F> {

  override fun MF(): Monad<F>
  override fun FF(): Functor<F> = MF()

  override fun <A> just(a: A): EitherT<L, F, A> =
    EitherT.just(MF(), a)

  override fun <A, B> EitherTOf<L, F, A>.map(f: (A) -> B): EitherT<L, F, B> =
    fix().map(MF(), f)
}

@extension
@undocumented
interface EitherTMonad<L, F> : Monad<EitherTPartialOf<L, F>>, EitherTApplicative<L, F> {

  override fun MF(): Monad<F>

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
interface EitherTApplicativeError<L, F> : ApplicativeError<EitherTPartialOf<L, F>, L>, EitherTApplicative<L, F> {

  override fun MF(): Monad<F>

  override fun <A> raiseError(e: L): Kind<EitherTPartialOf<L, F>, A> = EitherT.left(MF(), e)

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.handleErrorWith(f: (L) -> Kind<EitherTPartialOf<L, F>, A>): Kind<EitherTPartialOf<L, F>, A> =
    MF().run {
      value().flatMap {
        it.fold({ f(it).value() }, { MF().just(it.right()) })
      }.let(::EitherT)
    }
}

@extension
@undocumented
interface EitherTMonadError<L, F> : MonadError<EitherTPartialOf<L, F>, L>, EitherTApplicativeError<L, F>, EitherTMonad<L, F> {
  override fun MF(): Monad<F>
}

@extension
@undocumented
interface EitherTMonadThrow<L, F> : MonadThrow<EitherTPartialOf<L, F>>, EitherTMonad<L, F> {

  fun MT(): MonadThrow<F>
  override fun MF(): Monad<F> = MT()

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.handleErrorWith(f: (Throwable) -> Kind<EitherTPartialOf<L, F>, A>): Kind<EitherTPartialOf<L, F>, A> =
    MT().run { value().handleErrorWith { f(it).value() }.let(::EitherT) }

  override fun <A> raiseError(e: Throwable): Kind<EitherTPartialOf<L, F>, A> = EitherT.liftF(MT(), MT().raiseError(e))
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
  override fun MF(): Monad<F>
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

fun <L, F, R> EitherT.Companion.fx(M: MonadThrow<F>, c: suspend MonadThrowSyntax<EitherTPartialOf<L, F>>.() -> R): EitherT<L, F, R> =
  EitherT.monadThrow<L, F>(M).fx.monadThrow(c).fix()

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

@extension
interface EitherTMonadReader<L, F, D> : MonadReader<EitherTPartialOf<L, F>, D>, EitherTMonad<L, F> {
  fun MR(): MonadReader<F, D>
  override fun MF(): Monad<F> = MR()

  override fun ask(): Kind<EitherTPartialOf<L, F>, D> = EitherT.liftF(MR(), MR().ask())

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.local(f: (D) -> D): Kind<EitherTPartialOf<L, F>, A> =
    EitherT(MR().run { fix().value().local(f) })
}

@extension
interface EitherTMonadWriter<L, F, W> : MonadWriter<EitherTPartialOf<L, F>, W>, EitherTMonad<L, F> {
  fun MW(): MonadWriter<F, W>
  override fun MF(): Monad<F> = MW()

  override fun <A> Kind<EitherTPartialOf<L, F>, A>.listen(): Kind<EitherTPartialOf<L, F>, Tuple2<W, A>> =
    EitherT(MW().run { fix().value().listen().map { (w, e) -> e.map { w toT it } } })

  override fun <A> Kind<EitherTPartialOf<L, F>, Tuple2<(W) -> W, A>>.pass(): Kind<EitherTPartialOf<L, F>, A> =
    EitherT(MW().run {
      fix().value().map {
        it.fold({
          Tuple2({ w: W -> w }, it.left())
        }, {
          it.map(::Right)
        })
      }.pass()
    })

  override fun <A> writer(aw: Tuple2<W, A>): Kind<EitherTPartialOf<L, F>, A> = EitherT.liftF(MW(), MW().writer(aw))
}

@extension
interface EitherTMonadState<L, F, S> : MonadState<EitherTPartialOf<L, F>, S>, EitherTMonad<L, F> {
  fun MS(): MonadState<F, S>
  override fun MF(): Monad<F> = MS()

  override fun get(): Kind<EitherTPartialOf<L, F>, S> = EitherT.liftF(MS(), MS().get())
  override fun set(s: S): Kind<EitherTPartialOf<L, F>, Unit> = EitherT.liftF(MS(), MS().set(s))
}
