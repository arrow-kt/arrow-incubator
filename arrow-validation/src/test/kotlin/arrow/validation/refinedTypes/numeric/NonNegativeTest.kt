package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterEqual
import arrow.validation.test.lessThan
import arrow.validation.refinedTypes.numeric.validated.nonNegative.nonNegative
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class NonNegativeTest : UnitSpec() {
  init {

    "Should create NonNegative for every x >= 0" {
      forAll(Gen.greaterEqual(0)) { x: Int ->
        x.nonNegative(Int.order()).isValid
      }
    }

    "Should not create NonNegative for any x < 0" {
      forAll(Gen.lessThan(0)) { x: Int ->
        x.nonNegative(Int.order()).isInvalid
      }
    }
  }
}
