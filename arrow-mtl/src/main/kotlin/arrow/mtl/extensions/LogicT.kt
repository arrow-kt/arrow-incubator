package arrow.mtl.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.AndThen
import arrow.core.Either
import arrow.core.Eval
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.andThen
import arrow.extension
import arrow.mtl.ForLogicT
import arrow.mtl.LogicT
import arrow.mtl.LogicTPartialOf
import arrow.mtl.fix
import arrow.mtl.typeclasses.MonadReader
import arrow.mtl.typeclasses.MonadState
import arrow.mtl.typeclasses.MonadTrans
import arrow.typeclasses.Alternative
import arrow.typeclasses.Applicative
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.Foldable
import arrow.typeclasses.Functor
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.typeclasses.MonadLogic
import arrow.typeclasses.MonadPlus
import arrow.typeclasses.Monoid
import arrow.typeclasses.MonoidK
import arrow.typeclasses.SemigroupK
import arrow.mtl.ap as logicTAp
import arrow.mtl.handleErrorWith as logicTHandleErrorWith
import arrow.mtl.local as logicTLocal

@extension
interface LogicTFunctor<M> : Functor<LogicTPartialOf<M>> {
  override fun <A, B> Kind<LogicTPartialOf<M>, A>.map(f: (A) -> B): Kind<LogicTPartialOf<M>, B> =
    fix().map(f)
}

@extension
interface LogicTApplicative<M> : Applicative<LogicTPartialOf<M>>, LogicTFunctor<M> {
  override fun <A> just(a: A): Kind<LogicTPartialOf<M>, A> = LogicT.just(a)
  override fun <A, B> Kind<LogicTPartialOf<M>, A>.ap(ff: Kind<LogicTPartialOf<M>, (A) -> B>): Kind<LogicTPartialOf<M>, B> =
    logicTAp(ff)

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.map(f: (A) -> B): Kind<LogicTPartialOf<M>, B> =
    fix().map(f)
}

@extension
interface LogicTMonad<M> : Monad<LogicTPartialOf<M>>, LogicTApplicative<M> {
  override fun <A, B> Kind<LogicTPartialOf<M>, A>.flatMap(f: (A) -> Kind<LogicTPartialOf<M>, B>): Kind<LogicTPartialOf<M>, B> =
    fix().flatMap(f.andThen { it.fix() })

  override fun <A, B> tailRecM(a: A, f: (A) -> Kind<LogicTPartialOf<M>, Either<A, B>>): Kind<LogicTPartialOf<M>, B> =
    f(a).flatMap { it.fold({ tailRecM(it, f) }, { LogicT.just(it) }) }

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.ap(ff: Kind<LogicTPartialOf<M>, (A) -> B>): Kind<LogicTPartialOf<M>, B> =
    logicTAp(ff)

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.map(f: (A) -> B): Kind<LogicTPartialOf<M>, B> =
    fix().map(f)
}

@extension
interface LogicTSemigroupK<M> : SemigroupK<LogicTPartialOf<M>> {
  override fun <A> Kind<LogicTPartialOf<M>, A>.combineK(y: Kind<LogicTPartialOf<M>, A>): Kind<LogicTPartialOf<M>, A> =
    fix().orElse(y.fix())
}

@extension
interface LogicTMonoidK<M> : MonoidK<LogicTPartialOf<M>>, LogicTSemigroupK<M> {
  override fun <A> empty(): Kind<LogicTPartialOf<M>, A> = LogicT.empty()
}

@extension
interface LogicTAlternative<M> : Alternative<LogicTPartialOf<M>>, LogicTApplicative<M> {
  override fun <A> Kind<LogicTPartialOf<M>, A>.orElse(b: Kind<LogicTPartialOf<M>, A>): Kind<LogicTPartialOf<M>, A> =
    fix().orElse(b.fix())

  override fun <A> empty(): Kind<LogicTPartialOf<M>, A> = LogicT.empty()
}

@extension
interface LogicTMonadPlus<M> : MonadPlus<LogicTPartialOf<M>>, LogicTAlternative<M>, LogicTMonad<M>

@extension
interface LogicTMonadLogic<M> : MonadLogic<LogicTPartialOf<M>>, LogicTMonadPlus<M> {
  fun MM(): Monad<M>

  override fun <A> Kind<LogicTPartialOf<M>, A>.splitM(): Kind<LogicTPartialOf<M>, Option<Tuple2<Kind<LogicTPartialOf<M>, A>, A>>> =
    fix().splitM(MM())

  override fun <A> Kind<LogicTPartialOf<M>, A>.once(): Kind<LogicTPartialOf<M>, A> =
    fix().once()

  override fun <A> Kind<LogicTPartialOf<M>, A>.voidIfValue(): Kind<LogicTPartialOf<M>, Unit> =
    fix().voidIfValue()
}

@extension
interface LogicTFoldable<M> : Foldable<LogicTPartialOf<M>> {
  fun MM(): Monad<M>
  fun FM(): Foldable<M>

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.foldMap(MN: Monoid<B>, f: (A) -> B): B =
    fix().runLogicT({ a, mxsEval ->
      mxsEval.map { mxs -> MM().run { mxs.map { xs -> MN.run { f(a).combine(xs) } } } } },
      Eval.now(MM().just(MN.empty()))
    ).let { FM().run { it.fold(MN) } }

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.foldLeft(b: B, f: (B, A) -> B): B =
    foldMap(
      // this is a composed monoid of Dual and Endo. TODO add those to core individually?
      object: Monoid<AndThen<B, B>> {
        override fun empty(): AndThen<B, B> = AndThen.id()
        override fun AndThen<B, B>.combine(b: AndThen<B, B>): AndThen<B, B> = b.andThen(this)
      },
      { AndThen { b -> f(b, it) } } // TODO I have no idea if this is stacksafe
    ).invoke(b)

  override fun <A, B> Kind<LogicTPartialOf<M>, A>.foldRight(lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> =
    foldMap(
      // this is Endo but with AndThen TODO change endo in core?
      object: Monoid<AndThen<Eval<B>, Eval<B>>> {
        override fun empty(): AndThen<Eval<B>, Eval<B>> = AndThen.id()
        override fun AndThen<Eval<B>, Eval<B>>.combine(b: AndThen<Eval<B>, Eval<B>>): AndThen<Eval<B>, Eval<B>> =
          andThen(b)
      },
      { AndThen { b -> f(it, b) } } // TODO I have no idea if this is stacksafe
    ).invoke(lb)
}

@extension
interface LogicTMonadTrans : MonadTrans<ForLogicT> {
  override fun <G, A> Kind<G, A>.liftT(MG: Monad<G>): Kind2<ForLogicT, G, A> =
    LogicT.lift(MG, this)
}

@extension
interface LogicTMonadReader<M, D> : MonadReader<LogicTPartialOf<M>, D>, LogicTMonad<M> {
  fun MR(): MonadReader<M, D>
  override fun ask(): Kind<LogicTPartialOf<M>, D> = LogicT.lift(MR(), MR().ask())
  override fun <A> Kind<LogicTPartialOf<M>, A>.local(f: (D) -> D): Kind<LogicTPartialOf<M>, A> =
    logicTLocal(MR(), f)
}

@extension
interface LogicTMonadState<M, S> : MonadState<LogicTPartialOf<M>, S>, LogicTMonad<M> {
  fun MS(): MonadState<M, S>
  override fun get(): Kind<LogicTPartialOf<M>, S> = LogicT.lift(MS(), MS().get())
  override fun set(s: S): Kind<LogicTPartialOf<M>, Unit> = LogicT.lift(MS(), MS().set(s))
}

@extension
interface LogicTApplicativeError<M, E> : ApplicativeError<LogicTPartialOf<M>, E>, LogicTApplicative<M> {
  fun ME(): MonadError<M, E>

  override fun <A> raiseError(e: E): Kind<LogicTPartialOf<M>, A> = LogicT.lift(ME(), ME().raiseError(e))

  override fun <A> Kind<LogicTPartialOf<M>, A>.handleErrorWith(f: (E) -> Kind<LogicTPartialOf<M>, A>): Kind<LogicTPartialOf<M>, A> =
    logicTHandleErrorWith(ME(), f)
}

@extension
interface LogicTMonadError<M, E> : MonadError<LogicTPartialOf<M>, E>, LogicTApplicativeError<M, E>, LogicTMonad<M> {
  override fun ME(): MonadError<M, E>
}
