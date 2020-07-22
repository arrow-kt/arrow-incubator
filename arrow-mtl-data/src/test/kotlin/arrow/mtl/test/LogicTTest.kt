package arrow.mtl.test

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.SequenceK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.sequencek.foldable.foldable
import arrow.core.k
import arrow.core.value
import arrow.mtl.LogicT
import arrow.mtl.extensions.logict.monadLogic.monadLogic
import arrow.mtl.fix
import arrow.syntax.collections.tail
import arrow.typeclasses.MonadLogic
import arrow.typeclasses.altFromList
import arrow.typeclasses.altSum

fun main() {
  fun <A> LogicT<ForId, A>.printList() = observeAllT(Id.monad()).value().also(::println)
  fun <A> LogicT<ForId, A>.printList(n: Int) = observeManyT(Id.monad(), n).value().also(::println)

  LogicT.monadLogic(Id.monad()).run {
    val t1 = zeroM<Int>()
    val t2 = just(10).plusM(just(20)).plusM(just(30))
    val t3 = listOf(10, 20, 30).altFromList(this)

    fun <F> MonadLogic<F>.odds(): Kind<F, Int> = just(1)
      // unit().flatMap makes the tail lazy so we don't stackoverflow on construction
      .plusM(unit().flatMap { odds().map { 2 + it } })

    odds().fix().observeT(Id.monad()).value().also(::println)
    odds().fix().printList(500)

    // diverges because of unfair interleaving with plusM
    /*
    fx.monad {
      val x = odds().plusM(t3).bind()
      if (x.rem(2) == 0)
        x
      else zeroM<Int>().bind()
    }.fix().take(1).printList()
     */
    // does not diverge because interleave is fair
    fx.monad {
      val x = odds().interleave(t3).bind()
      if (x.rem(2) == 0)
        x
      else zeroM<Int>().bind()
    }.fix().printList(1)

    fun <F> MonadLogic<F>.oddsPlus(n: Int): Kind<F, Int> = odds().map { it + n }

    // diverges because flatMap is unfair
    /*
    fx.monad {
      val x = (just(0).plusM(just(1))).flatMap { oddsPlus(it) }.bind()
      if (x.rem(2) == 0)
        x
      else zeroM<Int>().bind()
    }.fix().printList(1)
     */

    // does not diverge because unweave is fair
    fx.monad {
      val x = (just(0).plusM(just(1))).unweave { oddsPlus(it) }.bind()
      if (x.rem(2) == 0)
        x
      else zeroM<Int>().bind()
    }.fix().printList(5)

    fun <F> MonadLogic<F>.iota(n: Int): Kind<F, Int> =
      (1..n).asSequence().k().map { just(it) }.altSum(this, SequenceK.foldable())

    // Find all non-prime odd numbers
    fx.monad {
      val n = odds().bind()
      guard(n > 1).bind()
      val d = iota(n - 1).bind()
      guard(d > 1 && n.rem(d) == 0).bind()
      n
    }.fix().printList(10)

    // Finding all prime odd numbers is a bit weirder unless we use the inbuilt ifThen for convenience
    fx.monad {
      val n = odds().bind()
      guard(n > 1).bind()

      iota(n - 1).flatMap { d -> guard(d > 1 && n.rem(d) == 0) }
        .ifThen(just(n)) { zeroM() }
        .bind()
    }.fix().printList(10)

    // Bogosort to show how once works
    fun <F, A> MonadLogic<F>.insert(l: List<A>, a: A): Kind<F, List<A>> =
      if (l.isEmpty()) just(listOf(a))
      else just(listOf(a) + l).plusM(unit().flatMap {
        insert(l.tail(), a).map { listOf(l.first()) + it }
      })

    fun <F, A> MonadLogic<F>.permute(l: List<A>): Kind<F, List<A>> =
      if (l.isEmpty()) just(emptyList<A>())
      else fx.monad {
        val p = permute(l.tail()).bind()
        insert(p, l.first()).bind()
      }

    fun <F> MonadLogic<F>.bogosort(l: List<Int>): Kind<F, List<Int>> =
      permute(l).flatMap { xs -> if (xs.sorted() == xs) just(xs) else zeroM() }

    // two solutions because of the two equal elements => unnecessary work
    bogosort(listOf(5, 0, 3, 4, 0, 1)).fix().printList()

    fun <F> MonadLogic<F>.bogosort2(l: List<Int>): Kind<F, List<Int>> =
      bogosort(l).once()

    // just one solution because once discarded all others. Will avoid extra work if the result in `F` is lazy
    bogosort2(listOf(5, 0, 3, 4, 0, 1)).fix().printList()

    // this also allows a faster version of our prime finder by using once to prune the condition
    fx.monad {
      val n = odds().bind()
      guard(n > 1).bind()

      iota(n - 1).flatMap { d -> guard(d > 1 && n.rem(d) == 0) }
        .once() // we only need one element that passes, because that already means n is not prime
        .ifThen(just(n)) { zeroM() }
        .bind()
    }.fix().printList(10)

    // this is a very common pattern which is why there is a shortcut
    fx.monad {
      val n = odds().bind()
      guard(n > 1).bind()

      iota(n - 1).flatMap { d -> guard(d > 1 && n.rem(d) == 0) }
        .voidIfValue() // shortcut for once().ifThen(unit()) { zeroM() }
        // currently bugged and diverges: https://github.com/arrow-kt/arrow-core/pull/56 fixes that
        .bind()

      n
    }.fix().printList(10)
  }
}

/*
class LogicTTest : UnitSpec() {
  init {

  }
}
*/
