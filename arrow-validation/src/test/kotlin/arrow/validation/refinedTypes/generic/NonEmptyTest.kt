package arrow.validation.refinedTypes.generic

import arrow.core.test.UnitSpec
import arrow.validation.test.nonEmptyString
import arrow.validation.refinedTypes.generic.validated.nonEmpty.nonEmpty
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class NonEmptyTest : UnitSpec() {
  init {
    "Should create NonEmpty for every string with length > 0" {
      forAll(Gen.nonEmptyString()) { s: String ->
        s.nonEmpty("").isValid
      }
    }

    "Should not create NonEmpty for empty strings" {
      "".nonEmpty("").isInvalid
    }
  }
}
