package arrow.mtl.extensions

import arrow.Kind
import arrow.Kind2
import arrow.core.AndThen
import arrow.core.Either
import arrow.core.Eval
import arrow.core.Eval.Now
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.left
import arrow.core.right
import arrow.core.toT
import arrow.extension
import arrow.mtl.ForStateT
import arrow.mtl.StateT
import arrow.mtl.StateTOf
import arrow.mtl.StateTPartialOf
import arrow.mtl.extensions.statet.monad.monad
import arrow.mtl.fix
import arrow.mtl.run
import arrow.mtl.typeclasses.MonadReader
import arrow.mtl.typeclasses.MonadState
import arrow.mtl.typeclasses.MonadTrans
import arrow.mtl.typeclasses.MonadWriter
import arrow.typeclasses.Alternative
import arrow.typeclasses.Applicative
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.Contravariant
import arrow.typeclasses.Decidable
import arrow.typeclasses.Divide
import arrow.typeclasses.Divisible
import arrow.typeclasses.Functor
import arrow.typeclasses.Monad
import arrow.typeclasses.MonadCombine
import arrow.typeclasses.MonadError
import arrow.typeclasses.MonadLogic
import arrow.typeclasses.MonadPlus
import arrow.typeclasses.MonadSyntax
import arrow.typeclasses.MonadThrow
import arrow.typeclasses.MonoidK
import arrow.typeclasses.SemigroupK
import arrow.undocumented

@extension
@undocumented
interface StateTFunctor<S, F> : Functor<StateTPartialOf<S, F>> {

  fun FF(): Functor<F>

  override fun <A, B> StateTOf<S, F, A>.map(f: (A) -> B): StateT<S, F, B> =
    fix().map(FF(), f)
}

@extension
@undocumented
interface StateTApplicative<S, F> : Applicative<StateTPartialOf<S, F>>, StateTFunctor<S, F> {

  fun MF(): Monad<F>

  override fun FF(): Functor<F> = MF()

  override fun <A, B> StateTOf<S, F, A>.map(f: (A) -> B): StateT<S, F, B> =
    fix().map(MF(), f)

  override fun <A> just(a: A): StateT<S, F, A> =
    StateT.just(MF(), a)

  override fun <A, B> StateTOf<S, F, A>.ap(ff: StateTOf<S, F, (A) -> B>): StateT<S, F, B> =
    fix().ap(MF(), ff)

  // Stacksafe only when `F` is stacksafe
  override fun <A, B> Kind<StateTPartialOf<S, F>, A>.apEval(ff: Eval<Kind<StateTPartialOf<S, F>, (A) -> B>>): Eval<Kind<StateTPartialOf<S, F>, B>> =
    fix().flatMap(MF()) { a -> ff.value().map { f -> f(a) } }.let(::Now)
}

@extension
@undocumented
interface StateTMonad<S, F> : Monad<StateTPartialOf<S, F>>, StateTApplicative<S, F> {

  override fun MF(): Monad<F>

  override fun <A, B> StateTOf<S, F, A>.map(f: (A) -> B): StateT<S, F, B> =
    fix().map(MF(), f)

  override fun <A, B> StateTOf<S, F, A>.flatMap(f: (A) -> StateTOf<S, F, B>): StateT<S, F, B> =
    fix().flatMap(MF(), f)

  override fun <A, B> tailRecM(a: A, f: (A) -> StateTOf<S, F, Either<A, B>>): StateT<S, F, B> =
    StateT.tailRecM(MF(), a, f)

  override fun <A, B> StateTOf<S, F, A>.ap(ff: StateTOf<S, F, (A) -> B>): StateT<S, F, B> =
    fix().ap(MF(), ff.fix())
}

@extension
@undocumented
interface StateTSemigroupK<S, F> : SemigroupK<StateTPartialOf<S, F>> {
  fun SS(): SemigroupK<F>

  override fun <A> StateTOf<S, F, A>.combineK(y: StateTOf<S, F, A>): StateT<S, F, A> =
    fix().combineK(SS(), y)
}

@extension
@undocumented
interface StateTMonoidK<S, F> : MonoidK<StateTPartialOf<S, F>>, StateTSemigroupK<S, F> {
  fun MF(): Monad<F>
  fun MO(): MonoidK<F>
  override fun SS(): SemigroupK<F> = MO()

  override fun <A> empty(): Kind<StateTPartialOf<S, F>, A> = StateT.liftF(MF(), MO().empty<A>())
}

@extension
@undocumented
interface StateTApplicativeError<S, F, E> : ApplicativeError<StateTPartialOf<S, F>, E>, StateTApplicative<S, F> {

  fun ME(): MonadError<F, E>

  override fun FF(): Functor<F> = ME()

  override fun MF(): Monad<F> = ME()

  override fun <A> raiseError(e: E): StateTOf<S, F, A> = ME().run {
    StateT.liftF(this, raiseError(e))
  }

  override fun <A> StateTOf<S, F, A>.handleErrorWith(f: (E) -> StateTOf<S, F, A>): StateT<S, F, A> =
    StateT(AndThen.id<S>().flatMap { s ->
      AndThen(fix().runF).andThen {
        ME().run {
          it.handleErrorWith { e -> f(e).run(s) }
        }
      }
    })
}

@extension
@undocumented
interface StateTMonadError<S, F, E> : MonadError<StateTPartialOf<S, F>, E>, StateTApplicativeError<S, F, E>, StateTMonad<S, F> {

  override fun ME(): MonadError<F, E>

  override fun MF(): Monad<F> = ME()
}

@extension
@undocumented
interface StateTMonadThrow<S, F> : MonadThrow<StateTPartialOf<S, F>>, StateTMonadError<S, F, Throwable> {
  override fun ME(): MonadError<F, Throwable>
}

@extension
@undocumented
interface StateTContravariantInstance<S, F> : Contravariant<StateTPartialOf<S, F>> {

  fun CF(): Contravariant<F>

  override fun <A, B> Kind<StateTPartialOf<S, F>, A>.contramap(f: (B) -> A): Kind<StateTPartialOf<S, F>, B> =
    StateT(AndThen(fix().runF).andThen { fa -> CF().run { fa.contramap { (s, b): Tuple2<S, B> -> s toT f(b) } } })
}

@extension
@undocumented
interface StateTDivideInstance<S, F> : Divide<StateTPartialOf<S, F>>, StateTContravariantInstance<S, F> {

  fun DF(): Divide<F>
  override fun CF(): Contravariant<F> = DF()

  override fun <A, B, Z> divide(fa: Kind<StateTPartialOf<S, F>, A>, fb: Kind<StateTPartialOf<S, F>, B>, f: (Z) -> Tuple2<A, B>): Kind<StateTPartialOf<S, F>, Z> =
    StateT(AndThen(fa.fix().runF).flatMap { fa ->
      AndThen(fb.fix().runF).andThen { fb ->
        DF().divide(fa, fb) { (s, z): Tuple2<S, Z> ->
          val (a, b) = f(z)
          (s toT a) toT (s toT b)
        }
      }
    })
}

@extension
@undocumented
interface StateTDivisibleInstance<S, F> : Divisible<StateTPartialOf<S, F>>, StateTDivideInstance<S, F> {
  fun DFF(): Divisible<F>
  override fun DF(): Divide<F> = DFF()

  override fun <A> conquer(): Kind<StateTPartialOf<S, F>, A> =
    StateT { DFF().conquer() }
}

@extension
@undocumented
interface StateTDecidableInstante<S, F> : Decidable<StateTPartialOf<S, F>>, StateTDivisibleInstance<S, F> {
  fun DFFF(): Decidable<F>
  override fun DFF(): Divisible<F> = DFFF()

  override fun <A, B, Z> choose(fa: Kind<StateTPartialOf<S, F>, A>, fb: Kind<StateTPartialOf<S, F>, B>, f: (Z) -> Either<A, B>): Kind<StateTPartialOf<S, F>, Z> =
    StateT(AndThen(fa.fix().runF).flatMap { fa ->
      AndThen(fb.fix().runF).andThen { fb ->
        DFFF().choose(fa, fb) { (s, z): Tuple2<S, Z> ->
          f(z).fold({ a ->
            (s toT a).left()
          }, { b ->
            (s toT b).right()
          })
        }
      }
    })
}

fun <S, F, A> StateT.Companion.fx(M: Monad<F>, c: suspend MonadSyntax<StateTPartialOf<S, F>>.() -> A): StateT<S, F, A> =
  StateT.monad<S, F>(M).fx.monad(c).fix()

@extension
interface StateTMonadCombine<S, F> : MonadCombine<StateTPartialOf<S, F>>, StateTMonad<S, F>, StateTAlternative<S, F> {

  fun MC(): MonadCombine<F>
  override fun AF(): Alternative<F> = MC()

  override fun MF(): Monad<F> = MC()

  override fun FF(): Monad<F> = MC()
  override fun MO(): MonoidK<F> = MC()

  override fun <A> empty(): Kind<StateTPartialOf<S, F>, A> = liftT(MC().empty())

  fun <A> liftT(ma: Kind<F, A>): StateT<S, F, A> = FF().run {
    StateT { s: S -> ma.map { a: A -> s toT a } }
  }
}

@extension
interface StateTAlternative<S, F> : Alternative<StateTPartialOf<S, F>>, StateTMonoidK<S, F>, StateTApplicative<S, F> {
  override fun MF(): Monad<F>
  override fun MO(): MonoidK<F> = AF()
  fun AF(): Alternative<F>

  override fun <A> empty(): Kind<StateTPartialOf<S, F>, A> = StateT.liftF(AF(), AF().empty<A>())

  override fun <A> Kind<StateTPartialOf<S, F>, A>.orElse(b: Kind<StateTPartialOf<S, F>, A>): Kind<StateTPartialOf<S, F>, A> =
    StateT(AndThen(fix().runF).flatMap { fa ->
      AndThen(b.fix().runF).andThen { fb ->
        AF().run { fa.orElse(fb) }
      }
    })

  // Note: This can stackoverflow if `F` is not stacksafe, which is a tradeoff for having true short-circuit
  override fun <A> Kind<StateTPartialOf<S, F>, A>.lazyOrElse(b: () -> Kind<StateTPartialOf<S, F>, A>): Kind<StateTPartialOf<S, F>, A> =
    StateT(AndThen.id<S>().flatMap { s ->
      AndThen(fix().runF).andThen { fa ->
        AF().run { fa.lazyOrElse { b().fix().run(s) } }
      }
    })

  override fun <A> StateTOf<S, F, A>.combineK(y: StateTOf<S, F, A>): StateT<S, F, A> =
    orElse(y).fix()
}

@extension
interface StateTMonadTrans<S> : MonadTrans<Kind<ForStateT, S>> {
  override fun <G, A> Kind<G, A>.liftT(MG: Monad<G>): Kind2<Kind<ForStateT, S>, G, A> =
    StateT.liftF(MG, this)
}

@extension
interface StateTMonadPlus<S, F> : MonadPlus<StateTPartialOf<S, F>>, StateTMonad<S, F>, StateTAlternative<S, F> {
  override fun MF(): Monad<F>
  override fun AF(): Alternative<F>
}

@extension
interface StateTMonadLogic<S, F> : MonadLogic<StateTPartialOf<S, F>>, StateTMonadPlus<S, F> {
  fun ML(): MonadLogic<F>
  override fun MF(): Monad<F> = ML()
  override fun AF(): Alternative<F> = ML()

  override fun <A> Kind<StateTPartialOf<S, F>, A>.splitM(): Kind<StateTPartialOf<S, F>, Option<Tuple2<Kind<StateTPartialOf<S, F>, A>, A>>> =
    this.fix().let { fa ->
      StateT(
        ML().run {
          AndThen.id<S>().flatMap { s ->
            AndThen(fa.runF).andThen {
              it.splitM().map { option ->
                option.fold({ s toT Option.empty<Tuple2<Kind<StateTPartialOf<S, F>, A>, A>>() }, { (fa, tupleSA) ->
                  val (s1, a) = tupleSA
                  s1 toT Option.just(StateT { _: S -> fa } toT a)
                })
              }
            }
          }
        }
      )
    }

  override fun <A> Kind<StateTPartialOf<S, F>, A>.interleave(other: Kind<StateTPartialOf<S, F>, A>): Kind<StateTPartialOf<S, F>, A> =
    (this.fix() to other.fix()).let { (left, right) ->
      StateT(
        ML().run {
          AndThen.id<S>().flatMap { s ->
            AndThen(left.runF).flatMap { fa ->
              AndThen(right.runF).andThen {
                fa.interleave(it)
              }
            }
          }
        }
      )
    }

  override fun <A, B> Kind<StateTPartialOf<S, F>, A>.unweave(ffa: (A) -> Kind<StateTPartialOf<S, F>, B>): Kind<StateTPartialOf<S, F>, B> =
    this.fix().let { fa ->
      StateT(
        ML().run {
          AndThen(fa.runF).andThen {
            it.unweave { (s1, a) ->
              ffa(a).run(s1)
            }
          }
        }
      )
    }

  override fun <A, B> Kind<StateTPartialOf<S, F>, A>.ifThen(fb: Kind<StateTPartialOf<S, F>, B>, ffa: (A) -> Kind<StateTPartialOf<S, F>, B>): Kind<StateTPartialOf<S, F>, B> =
    (this.fix() to fb.fix()).let { (left, right) ->
      StateT(
        ML().run {
          AndThen.id<S>().flatMap { s ->
            AndThen(left.runF).flatMap { fa ->
              AndThen(right.runF).andThen {
                fa.ifThen(it) { (s1, a) ->
                  ffa(a).run(s1)
                }
              }
            }
          }
        }
      )
    }

  override fun <A> Kind<StateTPartialOf<S, F>, A>.once(): Kind<StateTPartialOf<S, F>, A> =
    this.fix().let { fa ->
      StateT(
        ML().run {
          AndThen(fa.runF).andThen {
            it.once()
          }
        }
      )
    }
}

@extension
interface StateTMonadState<S, F> : MonadState<StateTPartialOf<S, F>, S>, StateTMonad<S, F> {

  override fun MF(): Monad<F>

  override fun get(): StateT<S, F, S> = StateT.get(MF())

  override fun set(s: S): StateT<S, F, Unit> = StateT.set(MF(), s)
}

@extension
interface StateTMonadReader<S, F, D> : MonadReader<StateTPartialOf<S, F>, D>, StateTMonad<S, F> {
  fun MR(): MonadReader<F, D>
  override fun MF(): Monad<F> = MR()

  override fun ask(): Kind<StateTPartialOf<S, F>, D> = StateT.liftF(MR(), MR().ask())
  override fun <A> Kind<StateTPartialOf<S, F>, A>.local(f: (D) -> D): Kind<StateTPartialOf<S, F>, A> =
    StateT(AndThen(fix().runF).andThen { MR().run { it.local(f) } })
}

@extension
interface StateTMonadWriter<S, F, W> : MonadWriter<StateTPartialOf<S, F>, W>, StateTMonad<S, F> {
  fun MW(): MonadWriter<F, W>
  override fun MF(): Monad<F> = MW()

  override fun <A> Kind<StateTPartialOf<S, F>, A>.listen(): Kind<StateTPartialOf<S, F>, Tuple2<W, A>> =
    StateT(AndThen(fix().runF).andThen { MW().run { it.listen().map { (w, sa) -> sa.a toT (w toT sa.b) } } })

  override fun <A> Kind<StateTPartialOf<S, F>, Tuple2<(W) -> W, A>>.pass(): Kind<StateTPartialOf<S, F>, A> =
    StateT(AndThen(fix().runF).andThen {
      MW().run {
        it.map { (s, wfa) ->
          val (wf, a) = wfa
          wf toT (s toT a)
        }.pass()
      }
    })

  override fun <A> writer(aw: Tuple2<W, A>): Kind<StateTPartialOf<S, F>, A> = StateT.liftF(MW(), MW().writer(aw))
}
