package arrow.mtl.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.Eval
import arrow.extension
import arrow.mtl.ForOption2
import arrow.mtl.Option2
import arrow.mtl.Option2Of
import arrow.mtl.Option2PartialOf
import arrow.mtl.ap
import arrow.mtl.extensions.option2.eq.eq
import arrow.mtl.fix
import arrow.mtl.flatMap
import arrow.mtl.fold
import arrow.mtl.show
import arrow.typeclasses.Applicative
import arrow.typeclasses.Apply
import arrow.typeclasses.Bifunctor
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.EqK2
import arrow.typeclasses.Functor
import arrow.typeclasses.Monad
import arrow.typeclasses.Monoid
import arrow.typeclasses.Semigroup
import arrow.typeclasses.Show
import me.eugeniomarletti.kotlin.metadata.shadow.utils.addToStdlib.safeAs
import arrow.mtl.bimap as option2Bimap
import arrow.mtl.map as option2Map

@extension
interface Option2Functor<L> : Functor<Option2PartialOf<L>> {
  override fun <A, B> Option2Of<L, A>.map(f: (A) -> B): Option2<L, B> = option2Map(f)
}

@extension
interface Option2Bifunctor : Bifunctor<ForOption2> {

  override fun <A, B, C, D> Kind2<ForOption2, A, B>.bimap(fl: (A) -> C, fr: (B) -> D): Kind2<ForOption2, C, D> = option2Bimap(fl, fr)
}

@extension
interface Option2Eq<L, R> : Eq<Option2<L, R>> {

  fun EQL(): Eq<L>

  fun EQR(): Eq<R>

  override fun Option2<L, R>.eqv(b: Option2<L, R>): Boolean = when (this) {
    is Option2.None -> b is Option2.None
    is Option2.Some -> b.safeAs<Option2.Some<L, R>>()?.let { (a2, b2) ->
      EQL().run { a.eqv(a2) } && EQR().run { this@eqv.b.eqv(b2) }
    } ?: false
  }
}

@extension
interface Option2EqK<A> : EqK<Option2PartialOf<A>> {
  fun EQA(): Eq<A>

  override fun <B> Kind<Option2PartialOf<A>, B>.eqK(other: Kind<Option2PartialOf<A>, B>, EQ: Eq<B>): Boolean =
    Option2.eq(EQA(), EQ).run {
      this@eqK.fix().eqv(other.fix())
    }
}

@extension
interface Option2EqK2 : EqK2<ForOption2> {
  override fun <A, B> Kind2<ForOption2, A, B>.eqK(other: Kind2<ForOption2, A, B>, EQA: Eq<A>, EQB: Eq<B>): Boolean =
    (this.fix() to other.fix()).let {
      Option2.eq(EQA, EQB).run {
        it.first.eqv(it.second)
      }
    }
}

@extension
interface Option2Apply<L> : Apply<Option2PartialOf<L>>, Option2Functor<L> {

  fun SL(): Semigroup<L>

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> Kind<Option2PartialOf<L>, A>.ap(ff: Kind<Option2PartialOf<L>, (A) -> B>): Option2<L, B> =
    ap(SL(), ff)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> Kind<Option2PartialOf<L>, A>.apEval(ff: Eval<Kind<Option2PartialOf<L>, (A) -> B>>): Eval<Kind<Option2PartialOf<L>, B>> =
    fold(
      { Eval.now(Option2.None) },
      { l, b -> ff.map { it.option2Bimap({ ll -> SL().run { l + ll } }, { f -> f(b) }) } }
    )
}

@extension
interface Option2Applicative<L> : Applicative<Option2PartialOf<L>>, Option2Apply<L> {

  fun ML(): Monoid<L>

  override fun SL(): Semigroup<L> = ML()

  override fun <A> just(a: A): Option2<L, A> = Option2(ML().empty(), a)

  override fun <A, B> Kind<Option2PartialOf<L>, A>.map(f: (A) -> B): Option2<L, B> = option2Map(f)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> Kind<Option2PartialOf<L>, A>.ap(ff: Kind<Option2PartialOf<L>, (A) -> B>): Option2<L, B> = ap(SL(), ff)
}

@extension
interface Option2Monad<L> : Monad<Option2PartialOf<L>>, Option2Applicative<L> {

  override fun SL(): Semigroup<L>

  override fun ML(): Monoid<L>

  override fun <A, B> Kind<Option2PartialOf<L>, A>.map(f: (A) -> B): Option2<L, B> = option2Map(f)

  override fun <A, B> Kind<Option2PartialOf<L>, A>.flatMap(f: (A) -> Kind<Option2PartialOf<L>, B>): Option2<L, B> = flatMap(SL(), f)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> Kind<Option2PartialOf<L>, A>.ap(ff: Kind<Option2PartialOf<L>, (A) -> B>): Option2<L, B> = ap(SL(), ff)

  override fun <A, B> tailRecM(a: A, f: (A) -> Option2Of<L, Either<A, B>>): Option2<L, B> =
    Option2.tailRecM(a, f, SL())
}

@extension
interface Option2Show<A, B> : Show<Option2Of<A, B>> {
  fun SA(): Show<A>
  fun SB(): Show<B>
  override fun Option2Of<A, B>.show(): String = show(SA(), SB())
}
