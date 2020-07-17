package arrow.mtl.test

import arrow.core.toT
import arrow.core.test.UnitSpec
import arrow.mtl.accum
import arrow.mtl.evalAccum
import arrow.mtl.execAccum
import arrow.mtl.mapAccum
import arrow.mtl.runAccum
import io.kotest.property.Arb
import io.kotest.property.forAll
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

class AccumTest : UnitSpec() {
  init {
    "accum" {
      forAll(Arb.string(), Arb.int(), Arb.string()) { s, a, arg ->

        val ac = accum { _: String ->
          s toT a
        }

        ac.evalAccum(arg) shouldBe a
        ac.execAccum(arg) shouldBe s
        ac.runAccum(arg) shouldBe (s toT a)

        val rs = ac.mapAccum { (s1, a1) ->
          s1 toT "$a1"
        }.evalAccum(arg)

        rs == "$a"
      }
    }
  }
}
