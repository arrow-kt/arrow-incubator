package arrow.mtl

import arrow.Kind
import arrow.core.AndThen
import arrow.core.Eval
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.none
import arrow.core.some
import arrow.core.toT
import arrow.higherkind
import arrow.typeclasses.Monad

typealias SuccessFn<M, A, R> = (Tuple2<A, Eval<Kind<M, R>>>) -> Eval<Kind<M, R>>

// Why does this not work? Well because the empty part is fully strict.
// Solution: Wrap Kind<M, Any?> in Eval. Maybe this also adds enough trampolining to get rid of AndThen...
@higherkind
class LogicT<M, A>(
  internal val unLogicT: AndThen<Tuple2<SuccessFn<M, A, Any?>, Eval<Kind<M, Any?>>>, Eval<Kind<M, Any?>>>
): LogicTOf<M, A> {

  fun <B> map(f: (A) -> B): LogicT<M, B> =
    LogicT(unLogicT.compose { (fn, xs) ->
      AndThen(fn).compose<Tuple2<A, Eval<Kind<M, Any?>>>> { (a, e) -> f(a) toT e } toT xs
    })

  fun <B> flatMap(f: (A) -> LogicT<M, B>): LogicT<M, B> =
    LogicT(unLogicT.compose { (fn, xs) ->
      AndThen.id<Tuple2<A, Eval<Kind<M, Any?>>>>().flatMap { (a, _) ->
        f(a).unLogicT.compose<Tuple2<A, Eval<Kind<M, Any?>>>> { (_, e) -> fn toT e }
      } toT xs
    })

  fun orElse(other: LogicT<M, A>): LogicT<M, A> =
    LogicT(AndThen.id<Tuple2<SuccessFn<M, A, Any?>, Eval<Kind<M, Any?>>>>().flatMap { (fn, _) ->
      unLogicT.composeF(other.unLogicT.andThen { fn toT it })
    })

  // let the unsafe casts begin
  fun <R> runLogicT(f: (A, Eval<Kind<M, R>>) -> Eval<Kind<M, R>>, empty: Eval<Kind<M, R>>): Kind<M, R> =
    unLogicT((AndThen<Tuple2<A, Eval<Kind<M, R>>>, Eval<Kind<M, R>>> {
      f(it.a, it.b)
    } toT empty) as Tuple2<SuccessFn<M, A, Any?>, Eval<Kind<M, Any?>>>).value() as Kind<M, R>

  fun observeT(MM: Monad<M>): Kind<M, Option<A>> =
    runLogicT({ a, _ -> Eval.now(MM.just(a.some())) }, Eval.now(MM.just(none())))

  // Uses List to avoid stackoverflows on sequence. Since we don't have any lazy monads anyway there is no point using
  //  a lazy structure because all effects will be evaluated together with all values anyway.
  fun observeAllT(MM: Monad<M>): Kind<M, List<A>> =
    runLogicT({ a, mxsEval ->
      mxsEval.map { MM.run { it.map { listOf(a) + it } } }
    }, Eval.now(MM.just(emptyList())))

  fun observeManyT(MM: Monad<M>, n: Int): Kind<M, List<A>> = when {
    n <= 0 -> MM.just(emptyList())
    n == 1 -> runLogicT({ a, _ -> Eval.now(MM.just(listOf(a))) }, Eval.now(MM.just(emptyList())))
    else -> splitM(MM).runLogicT({ splits, _ ->
      splits.fold({
        Eval.now(MM.just(emptyList()))
      }, { (rem, a) ->
        Eval.now(MM.run { rem.observeManyT(MM, n - 1).map { listOf(a) + it } })
      })
    }, Eval.now(MM.just(emptyList())))
  }

  fun splitM(MM: Monad<M>): LogicT<M, Option<Tuple2<LogicT<M, A>, A>>> =
    lift(MM, runLogicT({ a, mxsEval ->
      Eval.now(MM.just((liftEval(MM, mxsEval).flatMap { reflect(it) } toT a).some()))
    }, Eval.now(MM.just(none()))))

  fun once(): LogicT<M, A> = LogicT(unLogicT.compose { (fn, xs) ->
    AndThen(fn).compose<Tuple2<A, Eval<Kind<M, Any?>>>> { (a, _) -> a toT xs } toT xs
  })

  fun voidIfValue(): LogicT<M, Unit> =
    LogicT(AndThen.id<Tuple2<SuccessFn<M, Unit, Any?>, Eval<Kind<M, Any?>>>>().flatMap { (fn, xs) ->
      // "run" fn in a stacksafe way to obtain Kind<M, Unit>
      AndThen(fn).compose<Tuple2<SuccessFn<M, Unit, Any?>, Eval<Kind<M, Any?>>>> { Unit toT xs }.flatMap { ma ->
        // new choice function that always goes xs and feed in ma
        unLogicT.compose<Tuple2<SuccessFn<M, Unit, Any?>, Eval<Kind<M, Any?>>>> { (_, _) ->
          AndThen<Tuple2<A, Eval<Kind<M, Any?>>>, Eval<Kind<M, Any?>>> { xs } toT ma
        }
      }
    })

  companion object {
    fun <M, A> empty(): LogicT<M, A> = LogicT(AndThen { (_, xs) -> xs })

    fun <M, A> just(a: A): LogicT<M, A> = LogicT(AndThen { (fn, xs) -> Eval.later {}.flatMap { fn(a toT xs) } })

    // lifting is only stacksafe through stacksafe monads
    fun <M, A> lift(MM: Monad<M>, fa: Kind<M, A>): LogicT<M, A> = LogicT(AndThen { (fn, xs) ->
      MM.run { Eval.later { fa.flatMap { fn(it toT xs).value() } } }
    })

    fun <M, A> liftEval(MM: Monad<M>, faEval: Eval<Kind<M, A>>): LogicT<M, A> = LogicT(AndThen { (fn, xs) ->
      MM.run { faEval.map { it.flatMap { fn(it toT xs).value() } } }
    })
  }
}

fun <M, A, B> LogicTOf<M, A>.ap(ff: LogicTOf<M, (A) -> B>): LogicT<M, B> =
  LogicT(fix().unLogicT.compose { (fn, xs) ->
    AndThen.id<Tuple2<A, Eval<Kind<M, Any?>>>>().flatMap { (a, _) ->
      ff.fix().unLogicT.compose<Tuple2<A, Eval<Kind<M, Any?>>>> { (_, e) ->
        AndThen(fn).compose<Tuple2<(A) -> B, Eval<Kind<M, Any?>>>> { (f, e1) -> f(a) toT e1 } toT e
      }
    } toT xs
  })

fun <M, A> reflect(opt: Option<Tuple2<LogicT<M, A>, A>>): LogicT<M, A> =
  opt.fold({ LogicT.empty() }, { (rem, a) -> LogicT.just<M, A>(a).orElse(rem) })
