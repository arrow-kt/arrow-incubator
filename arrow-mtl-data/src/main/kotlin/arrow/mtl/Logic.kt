package arrow.mtl

import arrow.core.Eval
import arrow.core.ForId
import arrow.core.Id
import arrow.core.Option
import arrow.core.extensions.id.monad.monad
import arrow.core.value
import arrow.typeclasses.MonadLogic

typealias Logic<A> = LogicT<ForId, A>

typealias ForLogic = LogicTPartialOf<ForId>

fun <A, R> Logic<A>.runLogic(f: (A, R) -> R, empty: R): R =
  runLogicT({ a, mxsEval -> mxsEval.map { Id(f(a, it.value())) } }, Eval.now(Id.just(empty))).value()

fun <A> Logic<A>.observe(): Option<A> = observeT(Id.monad()).value()

fun <A> Logic<A>.observeAll(): List<A> = observeAllT(Id.monad()).value()

// If observeAllT is stacksafe and lazy enough this will be faster than observeManyT
// This only works if the monad itself is lazy and in kotlin Id is not.
// fun <A> Logic<A>.observeMany(n: Int): Sequence<A> = observeAll().take(n)
fun <A> Logic<A>.observeMany(n: Int): List<A> = observeManyT(Id.monad(), n).value()
