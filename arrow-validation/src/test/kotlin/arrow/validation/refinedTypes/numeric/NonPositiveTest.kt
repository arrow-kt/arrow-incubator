package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterThan
import arrow.validation.test.lessEqual
import arrow.validation.refinedTypes.numeric.validated.nonPositive.nonPositive
import io.kotest.property.Arb
import io.kotest.property.forAll

class NonPositiveTest : UnitSpec() {
  init {

    "Should create NonPositive for every x <= 0" {
      forAll(Arb.lessEqual(0)) { x: Int ->
        x.nonPositive(Int.order()).isValid
      }
    }

    "Should not create NonPositive for any x > 0" {
      forAll(Arb.greaterThan(0)) { x: Int ->
        x.nonPositive(Int.order()).isInvalid
      }
    }
  }
}
