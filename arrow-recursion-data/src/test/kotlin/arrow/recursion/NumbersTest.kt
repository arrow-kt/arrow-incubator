package arrow.recursion

import arrow.core.Eval
import arrow.core.Option
import arrow.core.extensions.eq
import arrow.core.extensions.monoid
import arrow.core.extensions.option.foldable.fold
import arrow.core.extensions.option.foldable.foldRight
import arrow.core.extensions.option.traverse.traverse
import arrow.recursion.extensions.birecursive
import arrow.core.test.UnitSpec
import arrow.core.test.generators.intSmall
import arrow.recursion.test.BirecursiveLaws
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map

class IntBirecursive : UnitSpec() {
  init {

    testLaws(
      BirecursiveLaws.laws(
        Option.traverse(),
        Int.birecursive(),
        Arb.intSmall().filter { it in 0..100 },
        Arb.constant(5000),
        Int.eq(),
        {
          it.fold(Int.monoid())
        },
        {
          it.foldRight(Eval.now(0)) { v, acc -> acc.map { it + v } }
        },
        Int.birecursive().project(),
        {
          Eval.later {
            Int.birecursive().run {
              it.projectT()
            }
          }
        }
      )
    )
  }
}

class LongBirecursive : UnitSpec() {
  init {
    testLaws(
      BirecursiveLaws.laws(
        Option.traverse(),
        Long.birecursive(),
        Arb.intSmall().filter { it in 0..100 }.map { it.toLong() },
        Arb.constant(5000L),
        Long.eq(),
        {
          it.fold(Int.monoid())
        },
        {
          it.foldRight(Eval.now(0)) { v, acc -> acc.map { it + v } }
        },
        Int.birecursive().project(),
        {
          Eval.later {
            Int.birecursive().run {
              it.projectT()
            }
          }
        }
      )
    )
  }
}
