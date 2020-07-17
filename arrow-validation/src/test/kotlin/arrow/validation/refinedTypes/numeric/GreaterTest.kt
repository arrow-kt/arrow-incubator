package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterThan
import arrow.validation.test.lessEqual
import arrow.validation.refinedTypes.numeric.validated.greater.greater
import io.kotest.property.Arb
import io.kotest.property.forAll

class GreaterTest : UnitSpec() {
  init {
    val min = 100

    "Can create Greater for every number greater than the min defined by instace" {
      forAll(Arb.greaterThan(min)) { x: Int ->
        x.greater(Int.order(), min).isValid
      }
    }

    "Can not create Greater for any number less or equal than the min defined by instance" {
      forAll(Arb.lessEqual(min)) { x: Int ->
        x.greater(Int.order(), min).isInvalid
      }
    }
  }
}
