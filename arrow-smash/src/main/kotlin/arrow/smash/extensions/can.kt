@file:Suppress("UnusedImports")

package arrow.smash.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.Eval
import arrow.extension
import arrow.smash.Can
import arrow.smash.Can.Both
import arrow.smash.Can.Left
import arrow.smash.Can.Neither
import arrow.smash.Can.Right
import arrow.smash.CanOf
import arrow.smash.CanPartialOf
import arrow.smash.ForCan
import arrow.smash.ap
import arrow.smash.extensions.can.eq.eq
import arrow.smash.extensions.can.monad.monad
import arrow.smash.fix
import arrow.smash.flatMap
import arrow.smash.fold
import arrow.smash.toCan
import arrow.smash.toLeftCan
import arrow.typeclasses.Align
import arrow.typeclasses.Applicative
import arrow.typeclasses.Apply
import arrow.typeclasses.Bicrosswalk
import arrow.typeclasses.Bifoldable
import arrow.typeclasses.Bifunctor
import arrow.typeclasses.Bitraverse
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.EqK2
import arrow.typeclasses.Foldable
import arrow.typeclasses.Functor
import arrow.typeclasses.Hash
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadSyntax
import arrow.typeclasses.Monoid
import arrow.typeclasses.Semigroup
import arrow.typeclasses.Show
import arrow.typeclasses.Traverse

fun <L, R> Can<L, R>.combine(SGL: Semigroup<L>, SGR: Semigroup<R>, b: Can<L, R>): Can<L, R> =
  SGL.combine(SGR, this, b)

private fun <L, R> Semigroup<L>.combine(SGR: Semigroup<R>, a: Can<L, R>, b: Can<L, R>): Can<L, R> = SGR.run {
  when (a) {
    is Neither -> b
    is Left -> when (b) {
      is Neither -> a
      is Left -> Left(a.a + b.a)
      is Right -> Both(a.a, b.b)
      is Both -> Both(a.a + b.a, b.b)
    }
    is Right -> when (b) {
      is Neither -> a
      is Left -> Both(b.a, a.b)
      is Right -> Right(a.b + b.b)
      is Both -> Both(b.a, a.b + b.b)
    }
    is Both -> when (b) {
      is Neither -> a
      is Left -> Both(a.a + b.a, a.b)
      is Right -> Both(a.a, a.b + b.b)
      is Both -> Both(a.a + b.a, a.b + b.b)
    }
  }
}

@extension
interface CanSemigroup<L, R> : Semigroup<Can<L, R>> {

  fun SGL(): Semigroup<L>
  fun SGR(): Semigroup<R>

  override fun Can<L, R>.combine(b: Can<L, R>): Can<L, R> = fix().combine(SGL(), SGR(), b)
}

@extension
interface CanMonoid<L, R> : Monoid<Can<L, R>>, CanSemigroup<L, R> {
  fun MOL(): Monoid<L>
  fun MOR(): Monoid<R>

  override fun SGL(): Semigroup<L> = MOL()
  override fun SGR(): Semigroup<R> = MOR()

  override fun empty(): Can<L, R> = Neither
}

@extension
interface CanFunctor<L> : Functor<CanPartialOf<L>> {
  override fun <A, B> CanOf<L, A>.map(f: (A) -> B): Can<L, B> = fix().map(f)
}

@extension
interface CanBifunctor : Bifunctor<ForCan> {
  override fun <A, B, C, D> CanOf<A, B>.bimap(fl: (A) -> C, fr: (B) -> D): Can<C, D> =
    fix().bimap(fl, fr)
}

@extension
interface CanApply<L> : Apply<CanPartialOf<L>>, CanFunctor<L> {

  fun SL(): Semigroup<L>

  override fun <A, B> CanOf<L, A>.map(f: (A) -> B): Can<L, B> = fix().map(f)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> CanOf<L, A>.apEval(ff: Eval<CanOf<L, (A) -> B>>): Eval<Can<L, B>> =
    fold(
      ifNeither = { Eval.now(Neither) },
      ifLeft = { l -> Eval.now(Left(l)) },
      ifRight = { r -> ff.map { it.fix().map { f -> f(r) } } },
      ifBoth = { l, r ->
        ff.map { partial ->
          partial.fold(
            ifNeither = Can.Companion::neither,
            ifLeft = { ll -> SL().run { l + ll }.toLeftCan() },
            ifRight = { f -> Both(l, f(r)) },
            ifBoth = { ll, f -> Both(SL().run { l + ll }, f(r)) }
          )
        }
      }
    )

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> CanOf<L, A>.ap(ff: CanOf<L, (A) -> B>): Can<L, B> = ap(SL(), ff)
}

@extension
interface CanApplicative<L> : Applicative<CanPartialOf<L>>, CanApply<L> {

  override fun SL(): Semigroup<L>

  override fun <A> just(a: A): Can<L, A> = Right(a)

  override fun <A, B> CanOf<L, A>.map(f: (A) -> B): Can<L, B> = fix().map(f)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> CanOf<L, A>.ap(ff: CanOf<L, (A) -> B>): Can<L, B> =
    ap(SL(), ff)
}

@extension
interface CanMonad<L> : Monad<CanPartialOf<L>>, CanApplicative<L> {

  override fun SL(): Semigroup<L>

  override fun <A, B> CanOf<L, A>.map(f: (A) -> B): Can<L, B> = fix().map(f)

  @Suppress("OverridingDeprecatedMember")
  override fun <A, B> CanOf<L, A>.ap(ff: CanOf<L, (A) -> B>): Can<L, B> = ap(SL(), ff)

  override fun <A, B> CanOf<L, A>.flatMap(f: (A) -> CanOf<L, B>): Can<L, B> = flatMap(SL(), f)

  override fun <A, B> tailRecM(a: A, f: (A) -> CanOf<L, Either<A, B>>): Can<L, B> =
    Can.tailRecM(a, f, SL())
}

@extension
interface CanFoldable<L> : Foldable<CanPartialOf<L>> {

  override fun <A, B> CanOf<L, A>.foldLeft(b: B, f: (B, A) -> B): B =
    fix().foldLeft(b, f)

  override fun <A, B> CanOf<L, A>.foldRight(lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> =
    fix().foldRight(lb, f)
}

@extension
interface CanBifoldable : Bifoldable<ForCan> {
  override fun <A, B, C> CanOf<A, B>.bifoldLeft(c: C, f: (C, A) -> C, g: (C, B) -> C): C = fix().bifoldLeft(c, f, g)

  override fun <A, B, C> CanOf<A, B>.bifoldRight(c: Eval<C>, f: (A, Eval<C>) -> Eval<C>, g: (B, Eval<C>) -> Eval<C>): Eval<C> =
    fix().bifoldRight(c, f, g)
}

@extension
interface CanTraverse<L> : Traverse<CanPartialOf<L>>, CanFoldable<L> {

  override fun <G, A, B> CanOf<L, A>.traverse(AP: Applicative<G>, f: (A) -> Kind<G, B>): Kind<G, CanOf<L, B>> =
    AP.run { fold({ just(Neither) }, { just(Left(it)) }, { b -> f(b).map(::Right) }, { _, b -> f(b).map(::Right) }) }
}

@extension
interface CanBitraverse : Bitraverse<ForCan>, CanBifoldable {
  override fun <G, A, B, C, D> CanOf<A, B>.bitraverse(
    AP: Applicative<G>,
    f: (A) -> Kind<G, C>,
    g: (B) -> Kind<G, D>
  ): Kind<G, CanOf<C, D>> = AP.run {
    fold(
      ifNeither = { just(Neither) },
      ifLeft = { f(it).map(::Left) },
      ifRight = { g(it).map(::Right) },
      ifBoth = { a, b -> mapN(f(a), g(b)) { Both(it.a, it.b) } }
    )
  }
}

@extension
interface CanEq<in L, in R> : Eq<Can<L, R>> {

  fun EQL(): Eq<L>

  fun EQR(): Eq<R>

  override fun Can<L, R>.eqv(b: Can<L, R>): Boolean = when (this) {
    is Neither -> b is Neither
    is Left -> b is Left && EQL().run { a.eqv(b.a) }
    is Right -> b is Right && EQR().run { this@eqv.b.eqv(b.b) }
    is Both -> b is Both && EQL().run { a.eqv(b.a) } && EQR().run { this@eqv.b.eqv(b.b) }
  }
}

@extension
interface CanEqK<L> : EqK<CanPartialOf<L>> {
  fun EQL(): Eq<L>

  override fun <R> CanOf<L, R>.eqK(other: CanOf<L, R>, EQ: Eq<R>): Boolean =
    Can.eq(EQL(), EQ).run { this@eqK.fix().eqv(other.fix()) }
}

@extension
interface CanEqK2 : EqK2<ForCan> {
  override fun <A, B> Kind2<ForCan, A, B>.eqK(other: Kind2<ForCan, A, B>, EQA: Eq<A>, EQB: Eq<B>): Boolean =
    (this.fix() to other.fix()).let { (a, b) -> Can.eq(EQA, EQB).run { a.eqv(b) } }
}

@extension
interface CanShow<L, R> : Show<Can<L, R>> {
  fun SL(): Show<L>
  fun SR(): Show<R>
  override fun Can<L, R>.show(): String = show(SL(), SR())
}

@extension
interface CanHash<L, R> : Hash<Can<L, R>>, CanEq<L, R> {

  fun HL(): Hash<L>
  fun HR(): Hash<R>

  override fun EQL(): Eq<L> = HL()

  override fun EQR(): Eq<R> = HR()

  override fun Can<L, R>.hash(): Int = hash(HL(), HR())
}

fun <L, R> Can.Companion.fx(SL: Semigroup<L>, c: suspend MonadSyntax<CanPartialOf<L>>.() -> R): Can<L, R> =
  Can.monad(SL).fx.monad(c).fix()

@extension
interface CanBicrosswalk : Bicrosswalk<ForCan>, CanBifunctor, CanBifoldable {
  override fun <F, A, B, C, D> bicrosswalk(
    ALIGN: Align<F>,
    tab: Kind2<ForCan, A, B>,
    fa: (A) -> Kind<F, C>,
    fb: (B) -> Kind<F, D>
  ): Kind<F, Kind2<ForCan, C, D>> =
    tab.fold(
      ifNeither = { ALIGN.empty() },
      ifLeft = { ALIGN.run { fa(it).map(::Left) } },
      ifRight = { ALIGN.run { fb(it).map(::Right) } },
      ifBoth = { a, b -> ALIGN.alignWith(fa(a), fb(b)) { it.toCan() } }
    )
}
