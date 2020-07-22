package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.ForConst
import arrow.core.ForId
import arrow.core.ForListK
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.listk.eqK.eqK
import arrow.core.extensions.listk.monadLogic.monadLogic
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.eqK.eqK
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.MonadLogicLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.test.eq.eqK
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.generators.genK
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.ForKleisli
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import arrow.mtl.extensions.kleisli.alternative.alternative
import arrow.mtl.extensions.kleisli.applicative.applicative
import arrow.mtl.extensions.kleisli.divisible.divisible
import arrow.mtl.extensions.kleisli.monadLogic.monadLogic
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.kleisli.monadState.monadState
import arrow.mtl.extensions.kleisli.monadWriter.monadWriter
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadWriterLaws
import arrow.mtl.extensions.kleisli.functor.functor
import arrow.mtl.extensions.kleisli.monad.monad
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
      ConcurrentLaws.laws<KleisliPartialOf<Int, ForIO>>(
        Kleisli.concurrent(IO.concurrent()),
        Kleisli.timer(IO.concurrent()),
        Kleisli.functor(IO.functor()),
        Kleisli.applicative(IO.applicative()),
        Kleisli.monad(IO.monad()),
        Kleisli.genK(IO.genK()),
        ioEQK
      ),
      DivisibleLaws.laws(
        Kleisli.divisible<Int, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        Kleisli.genK<Int, ConstPartialOf<Int>>(Const.genK(Gen.int())),
        constEQK
      ),
      MonadLogicLaws.laws(
        Kleisli.monadLogic<Int, ForListK>(ListK.monadLogic()),
        Kleisli.genK<Int, ForListK>(ListK.genK()),
        Kleisli.eqK(ListK.eqK(), 0)
      ),
      MonadReaderLaws.laws<KleisliPartialOf<Int, ForId>, Int>(
        Kleisli.monadReader(Id.monad()),
        Kleisli.genK(Id.genK()),
        Gen.int(),
        Kleisli.eqK(Id.eqK(), 1),
        Int.eq()
      ),
      MonadWriterLaws.laws(
        Kleisli.monadWriter<Int, WriterTPartialOf<String, ForId>, String>(WriterT.monadWriter(Id.monad(), String.monoid())),
        String.monoid(), Gen.string(),
        Kleisli.genK<Int, WriterTPartialOf<String, ForId>>(WriterT.genK(Id.genK(), Gen.string())),
        Kleisli.eqK(WriterT.eqK(Id.eqK(), String.eq()), 1), String.eq()
      ),
      MonadStateLaws.laws(
        Kleisli.monadState<Int, StateTPartialOf<Int, ForId>, Int>(StateT.monadState(Id.monad())),
        Kleisli.genK<Int, StateTPartialOf<Int, ForId>>(StateT.genK(Id.genK(), Gen.int())),
        Gen.int(), Kleisli.eqK(StateT.eqK(Id.eqK(), Int.eq(), 0), 1), Int.eq()
      )
    )

    "andThen should continue sequence" {
      val kleisli: Kleisli<Int, ForId, Int> = Kleisli { a: Int -> Id(a) }

      kleisli.andThen(Id.monad(), Id(3)).run(0) shouldBe Id(3)

      kleisli.andThen(Id.monad()) { b -> Id(b + 1) }.run(0) shouldBe Id(1)
    }
  }
}
