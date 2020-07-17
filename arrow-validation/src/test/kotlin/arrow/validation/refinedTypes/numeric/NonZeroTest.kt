package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.eq
import arrow.core.test.UnitSpec
import arrow.core.test.generators.nonZeroInt
import arrow.validation.refinedTypes.numeric.validated.nonZero.nonZero
import io.kotest.property.Arb
import io.kotest.property.forAll

class NonZeroTest : UnitSpec() {
  init {

    "Can create NonZero from any number except 0" {
      forAll(Arb.nonZeroInt()) { x: Int ->
        x.nonZero(Int.eq()).isValid
      }
    }

    "Can not create NonZero from 0" {
      0.nonZero(Int.eq()).isInvalid
    }
  }
}
