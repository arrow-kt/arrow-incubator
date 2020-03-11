package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.ForConst
import arrow.core.ForId
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.Option
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.eqK.eqK
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.test.eq.eqK
import arrow.mtl.ForKleisli
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.extensions.kleisli.alternative.alternative
import arrow.mtl.extensions.kleisli.divisible.divisible
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe

class KleisliTest : UnitSpec() {

  init {
    val optionEQK = Kleisli.eqK(Option.eqK(), 0)

    val ioEQK: EqK<Kind<Kind<ForKleisli, Int>, ForIO>> = Kleisli.eqK(IO.eqK(), 1)

    val constEQK: EqK<Kind<Kind<ForKleisli, Int>, Kind<ForConst, Int>>> = Kleisli.eqK(Const.eqK(Int.eq()), 1)

    testLaws(
      AlternativeLaws.laws(
        Kleisli.alternative<Int, ForOption>(Option.alternative()),
        Kleisli.genK<Int, ForOption>(Option.genK()),
        optionEQK
      ),
      // ConcurrentLaws.laws<KleisliPartialOf<Int, ForIO>>(
      //   Kleisli.concurrent(IO.concurrent()),
      //   Kleisli.timer(IO.concurrent()),
      //   Kleisli.functor(IO.functor()),
      //   Kleisli.applicative(IO.applicative()),
      //   Kleisli.monad(IO.monad()),
      //   genK(IO.genK()),
      //   ioEQK
      // ),
      DivisibleLaws.laws(
        Kleisli.divisible<Int, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        Kleisli.genK<Int, ConstPartialOf<Int>>(Const.genK(Gen.int())),
        constEQK
      ),
      MonadReaderLaws.laws<KleisliPartialOf<Int, ForId>, Int>(
        Kleisli.monadReader(Id.monad()),
        Kleisli.genK(Id.genK()),
        Gen.int(),
        Kleisli.eqK(Id.eqK(), 1),
        Int.eq()
      )
    )

    "andThen should continue sequence" {
      val kleisli: Kleisli<Int, ForId, Int> = Kleisli { a: Int -> Id(a) }

      kleisli.andThen(Id.monad(), Id(3)).run(0) shouldBe Id(3)

      kleisli.andThen(Id.monad()) { b -> Id(b + 1) }.run(0) shouldBe Id(1)
    }
  }
}
