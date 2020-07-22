package arrow.mtl.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.AndThen
import arrow.core.Either
import arrow.core.Eval
import arrow.core.ForId
import arrow.core.Id
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.andThen
import arrow.core.extensions.id.monad.monad
import arrow.core.toT
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
import arrow.typeclasses.Functor
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadError
import arrow.typeclasses.MonadLogic
import arrow.typeclasses.MonadPlus
import arrow.typeclasses.MonoidK
import arrow.typeclasses.SemigroupK
import arrow.mtl.ap as logicTAp

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

fun LogicT.Companion.monadLogic(): MonadLogic<LogicTPartialOf<ForId>> = object: LogicTMonadLogic<ForId> {
  override fun MM(): Monad<ForId> = Id.monad()
}

@extension
interface LogicTMonadTrans : MonadTrans<ForLogicT> {
  override fun <G, A> Kind<G, A>.liftT(MG: Monad<G>): Kind2<ForLogicT, G, A> =
    LogicT.lift(MG, this)
}

/*
@extension
interface LogicTMonadReader<M, D> : MonadReader<LogicTPartialOf<M>, D>, LogicTMonad<M> {
  fun MR(): MonadReader<M, D>
  override fun ask(): Kind<LogicTPartialOf<M>, D> = LogicT.lift(MR(), MR().ask())
  override fun <A> Kind<LogicTPartialOf<M>, A>.local(f: (D) -> D): Kind<LogicTPartialOf<M>, A> =
    LogicT(AndThen { (fn, mxs) ->
      Eval.later {
        MR().run {
          ask().flatMap { d ->
            fix().let { l ->
              l.runLogicT({ a, mxs2Eval ->
                fn(a toT mxs2Eval).map { it.local { d } }
              }, mxs.map { it.local { d } })
            }.local(f)
          }
        }
      }.
    })
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
    LogicT(AndThen { (fn, mxs) ->
      ME().run {
        fun Kind<M, Any?>.handle(): Kind<M, Any?> =
          handleErrorWith { e -> f(e).fix().runLogicT({ a, mxs2 -> fn(a toT mxs2) }, mxs) }

        fix().runLogicT({ a, mxs2 -> fn(a toT mxs2.handle()) }, mxs).handle()
      }
    })
}
*/
