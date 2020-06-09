package arrow.mtl.test

import arrow.core.extensions.eq
import arrow.core.extensions.monoid
import arrow.core.extensions.semigroup
import arrow.core.extensions.show
import arrow.core.test.UnitSpec
import arrow.core.test.laws.BifunctorLaws
import arrow.core.test.laws.EqK2Laws
import arrow.core.test.laws.EqKLaws
import arrow.core.test.laws.MonadLaws
import arrow.core.test.laws.ShowLaws
import arrow.mtl.Option2
import arrow.mtl.extensions.option2.applicative.applicative
import arrow.mtl.extensions.option2.bifunctor.bifunctor
import arrow.mtl.extensions.option2.eq.eq
import arrow.mtl.extensions.option2.eqK.eqK
import arrow.mtl.extensions.option2.eqK2.eqK2
import arrow.mtl.extensions.option2.functor.functor
import arrow.mtl.extensions.option2.monad.monad
import arrow.mtl.extensions.option2.show.show
import arrow.mtl.test.generators.genK
import arrow.mtl.test.generators.genK2
import arrow.mtl.test.generators.option2
import io.kotlintest.properties.Gen

class Option2Test : UnitSpec() {

  init {

    testLaws(
      EqK2Laws.laws(Option2.eqK2(), Option2.genK2()),
      BifunctorLaws.laws(Option2.bifunctor(), Option2.genK2(), Option2.eqK2()),
      MonadLaws.laws(
        Option2.monad(Int.semigroup(), Int.monoid()),
        Option2.functor(),
        Option2.applicative(Int.monoid()),
        Option2.monad(Int.semigroup(), Int.monoid()),
        Option2.genK(Gen.int()),
        Option2.eqK(Int.eq())
      ),
      EqKLaws.laws(Option2.eqK(Int.eq()), Option2.genK(Gen.int())),
      ShowLaws.laws(Option2.show(Int.show(), String.show()), Option2.eq(Int.eq(), String.eq()), Gen.option2(Gen.int(), Gen.string()))
    )
  }
}
