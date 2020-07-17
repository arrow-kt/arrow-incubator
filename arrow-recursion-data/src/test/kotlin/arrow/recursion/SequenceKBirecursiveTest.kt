package arrow.recursion

import arrow.core.Eval
import arrow.core.SequenceK
import arrow.core.extensions.eq
import arrow.core.extensions.sequencek.eq.eq
import arrow.core.k
import arrow.recursion.extensions.listf.traverse.traverse
import arrow.recursion.extensions.sequencek.birecursive.birecursive
import arrow.recursion.pattern.ListF
import arrow.recursion.pattern.fix
import arrow.core.test.UnitSpec
import arrow.recursion.test.BirecursiveLaws
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map

class SequenceKBirecursiveTest : UnitSpec() {
  init {
    testLaws(
      BirecursiveLaws.laws(
        ListF.traverse(),
        SequenceK.birecursive(),
        Arb.list(Arb.int()).map { it.asSequence().k() },
        Arb.constant((0..10).asSequence().k()),
        SequenceK.eq(Int.eq()),
        {
          when (val l = it.fix()) {
            is ListF.NilF -> 0
            is ListF.ConsF -> l.a + l.tail
          }
        },
        {
          Eval.later {
            when (val l = it.fix()) {
              is ListF.NilF -> 0
              is ListF.ConsF -> l.a + l.tail
            }
          }
        },
        {
          when (it) {
            0 -> ListF.NilF()
            else -> ListF.ConsF(0, it - 1)
          }
        },
        {
          Eval.later {
            when (it) {
              0 -> ListF.NilF<Int, Int>()
              else -> ListF.ConsF(0, it - 1)
            }
          }
        }
      )
    )
  }
}
