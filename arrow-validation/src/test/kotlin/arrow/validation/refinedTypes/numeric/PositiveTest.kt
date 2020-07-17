package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterThan
import arrow.validation.test.lessEqual
import arrow.validation.refinedTypes.numeric.validated.positive.positive
import io.kotest.property.Arb
import io.kotest.property.forAll

class PositiveTest : UnitSpec() {
  init {

    "Should create Positive for every x > 0" {
      forAll(Arb.greaterThan(0)) { x: Int ->
        x.positive(Int.order()).isValid
      }
    }

    "Should not create Positive for any x <= 0" {
      forAll(Arb.lessEqual(0)) { x: Int ->
        x.positive(Int.order()).isInvalid
      }
    }
  }
}
