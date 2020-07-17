package arrow.recursion

import arrow.core.Eval
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.extensions.eq
import arrow.core.extensions.nonemptylist.eq.eq
import arrow.core.none
import arrow.core.some
import arrow.recursion.extensions.nonemptylist.birecursive.birecursive
import arrow.recursion.extensions.nonemptylistf.traverse.traverse
import arrow.recursion.pattern.NonEmptyListF
import arrow.recursion.pattern.fix
import arrow.core.test.UnitSpec
import arrow.recursion.test.BirecursiveLaws
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map

class NonEmptyListBirecursiveTest : UnitSpec() {
  init {
    testLaws(
      BirecursiveLaws.laws(
        NonEmptyListF.traverse(),
        NonEmptyList.birecursive(),
        Arb.list(Arb.int()).filter { it.isNotEmpty() }.map { Nel.fromListUnsafe(it) },
        Arb.constant(Nel.fromListUnsafe((0..5000).toList())),
        Nel.eq(Int.eq()),
        {
          it.fix().tail.fold({ 0 }, { it + 1 })
        },
        {
          Eval.now(it.fix().tail.fold({ 0 }, { it + 1 }))
        },
        {
          NonEmptyListF(it, when (it) {
            0 -> none()
            else -> (it - 1).some()
          })
        },
        {
          Eval.now(NonEmptyListF(it, when (it) {
            0 -> none()
            else -> (it - 1).some()
          }))
        }
      )
    )
  }
}
