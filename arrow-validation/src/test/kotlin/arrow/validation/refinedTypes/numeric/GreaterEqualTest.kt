package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterEqual
import arrow.validation.test.lessThan
import arrow.validation.refinedTypes.numeric.validated.greaterEqual.greaterEqual
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class GreaterEqualTest : UnitSpec() {
  init {

    val min = 100

    "Can create GreaterEqual for every number greater or equal than min defined by instance" {
      forAll(Gen.greaterEqual(min)) { x: Int ->
        x.greaterEqual(Int.order(), min).isValid
      }
    }

    "Can not create GreaterEqual for any number lesser than min defined by instance" {
      forAll(Gen.lessThan(min)) { x: Int ->
        x.greaterEqual(Int.order(), min).isInvalid
      }
    }
  }
}
