package arrow.validation.refinedTypes.generic

import arrow.core.test.UnitSpec
import arrow.validation.test.nonEmptyString
import arrow.validation.refinedTypes.generic.validated.nonEmpty.nonEmpty
import io.kotest.property.Arb
import io.kotest.property.forAll

class NonEmptyTest : UnitSpec() {
  init {
    "Should create NonEmpty for every string with length > 0" {
      forAll(Arb.nonEmptyString()) { s: String ->
        s.nonEmpty("").isValid
      }
    }

    "Should not create NonEmpty for empty strings" {
      "".nonEmpty("").isInvalid
    }
  }
}
