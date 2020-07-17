package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterEqual
import arrow.validation.test.lessThan
import arrow.validation.refinedTypes.numeric.validated.negative.negative
import io.kotest.property.Arb
import io.kotest.property.forAll

class NegativeTest : UnitSpec() {
  init {

    "Should create Negative for every x < 0" {
      forAll(Arb.lessThan(0)) { x: Int ->
        x.negative(Int.order()).isValid
      }
    }

    "Should not create Negative for any x >= 0" {
      forAll(Arb.greaterEqual(0)) { x: Int ->
        x.negative(Int.order()).isInvalid
      }
    }
  }
}
