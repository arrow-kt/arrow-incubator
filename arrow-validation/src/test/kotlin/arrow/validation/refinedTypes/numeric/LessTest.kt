package arrow.validation.refinedTypes.numeric

import arrow.core.extensions.order
import arrow.core.test.UnitSpec
import arrow.validation.test.greaterOrEqThan
import arrow.validation.test.lessThan
import arrow.validation.refinedTypes.numeric.validated.less.less
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class LessTest : UnitSpec() {
  init {
    val max = 100

    "Can create Less for every number less than max defined by instance" {
      forAll(Gen.lessThan(max)) { x: Int ->
        x.less(Int.order(), max).isValid
      }
    }

    "Can not create Less for every number greater or equal to max defined by instance" {
      forAll(Gen.greaterOrEqThan(max)) { x: Int ->
        x.less(Int.order(), max).isInvalid
      }
    }
  }
}
