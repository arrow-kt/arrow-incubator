package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterThan
import arrow.validation.test.lessEqual
import arrow.validation.refinedTypes.numeric.validated.lessEqual.lessEqual
import io.kotest.property.Arb
import io.kotest.property.forAll

class LessEqualTest : UnitSpec() {
  init {

    val max = 100

    "Can create LessEqual for every number less or equal than min defined by instance" {
      forAll(Arb.lessEqual(max)) { x: Int ->
        x.lessEqual(Int.order(), max).isValid
      }
    }

    "Can not create LessEqual for any number greater than min defined by instance" {
      forAll(Arb.greaterThan(max)) { x: Int ->
        x.lessEqual(Int.order(), max).isInvalid
      }
    }
  }
}
