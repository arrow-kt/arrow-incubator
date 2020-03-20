package arrow.mtl.test

import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.ForId
import arrow.core.ForListK
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.listk.alternative.alternative
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.listk.eq.eq
import arrow.core.extensions.listk.eqK.eqK
import arrow.core.extensions.listk.monad.monad
import arrow.core.extensions.listk.monoid.monoid
import arrow.core.extensions.listk.monoidK.monoidK
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.monadFilter.monadFilter
import arrow.core.k
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.MonadFilterLaws
import arrow.core.test.laws.MonadPlusLaws
import arrow.core.test.laws.MonoidKLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.test.eq.eqK
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.extensions.WriterTEqK
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.writert.alternative.alternative
import arrow.mtl.extensions.writert.applicative.applicative
import arrow.mtl.extensions.writert.divisible.divisible
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.functor.functor
import arrow.mtl.extensions.writert.monad.monad
import arrow.mtl.extensions.writert.monadFilter.monadFilter
import arrow.mtl.extensions.writert.monadPlus.monadPlus
import arrow.mtl.extensions.writert.monadReader.monadReader
import arrow.mtl.extensions.writert.monadState.monadState
import arrow.mtl.extensions.writert.monadTrans.monadTrans
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.extensions.writert.monoidK.monoidK
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadTransLaws
import arrow.mtl.test.laws.MonadWriterLaws
import io.kotlintest.properties.Gen

class WriterTTest : UnitSpec() {

  fun ioEQK(): WriterTEqK<ListK<Int>, ForIO> = WriterT.eqK(IO.eqK(), ListK.eq(Int.eq()))

  fun optionEQK(): WriterTEqK<ListK<Int>, ForOption> = WriterT.eqK(Option.eqK(), ListK.eq(Int.eq()))

  fun constEQK(): WriterTEqK<ListK<Int>, ConstPartialOf<Int>> = WriterT.eqK(Const.eqK(Int.eq()), ListK.eq(Int.eq()))

  fun listEQK(): WriterTEqK<ListK<Int>, ForListK> = WriterT.eqK(ListK.eqK(), ListK.eq(Int.eq()))

  init {

    testLaws(
      MonadTransLaws.laws(
        WriterT.monadTrans(String.monoid()),
        Option.monad(),
        WriterT.monad(Option.monad(), String.monoid()),
        Option.genK(),
        WriterT.eqK(Option.eqK(), String.eq())
      ),
      AlternativeLaws.laws(
        WriterT.alternative(ListK.monoid<Int>(), Option.alternative()),
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK()
      ),
      DivisibleLaws.laws(
        WriterT.divisible<ListK<Int>, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        WriterT.genK(Const.genK(Gen.int()), Gen.list(Gen.int()).map { it.k() }),
        constEQK()
      ),
      // ConcurrentLaws.laws(
      //   WriterT.concurrent(IO.concurrent(), ListK.monoid<Int>()),
      //   WriterT.timer(IO.concurrent(), ListK.monoid<Int>()),
      //   WriterT.functor<ListK<Int>, ForIO>(IO.functor()),
      //   WriterT.applicative(IO.applicative(), ListK.monoid<Int>()),
      //   WriterT.monad(IO.monad(), ListK.monoid<Int>()),
      //   WriterT.genK(IO.genK(), Gen.list(Gen.int()).map { it.k() }),
      //   ioEQK()
      // ),
      MonoidKLaws.laws(
        WriterT.monoidK<ListK<Int>, ForListK>(ListK.monoidK()),
        WriterT.genK(ListK.genK(), Gen.list(Gen.int()).map { it.k() }),
        listEQK()
      ),

      MonadFilterLaws.laws(
        WriterT.monadFilter(Option.monadFilter(), ListK.monoid<Int>()),
        WriterT.functor<ListK<Int>, ForOption>(Option.functor()),
        WriterT.applicative(Option.applicative(), ListK.monoid<Int>()),
        WriterT.monad(Option.monad(), ListK.monoid<Int>()),
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK()
      ),

      MonadWriterLaws.laws(
        WriterT.monadWriter(Option.monad(), ListK.monoid<Int>()),
        ListK.monoid(),
        Gen.list(Gen.int()).map { it.k() },
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK(),
        ListK.eq(Int.eq())
      ),

      MonadFilterLaws.laws(
        WriterT.monadFilter(Option.monadFilter(), ListK.monoid<Int>()),
        WriterT.functor<ListK<Int>, ForOption>(Option.functor()),
        WriterT.applicative(Option.applicative(), ListK.monoid<Int>()),
        WriterT.monad(Option.monad(), ListK.monoid<Int>()),
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK()
      ),
      MonadPlusLaws.laws(
        WriterT.monadPlus(ListK.monad(), String.monoid(), ListK.alternative()),
        WriterT.genK(ListK.genK(), Gen.string()),
        WriterT.eqK(ListK.eqK(), String.eq())
      ),
      MonadReaderLaws.laws(
        WriterT.monadReader<String, KleisliPartialOf<Int, ForId>, Int>(Kleisli.monadReader(Id.monad()), String.monoid()),
        WriterT.genK(Kleisli.genK<Int, ForId>(Id.genK()), Gen.string()), Gen.int(),
        WriterT.eqK(Kleisli.eqK(Id.eqK(), 1), String.eq()), Int.eq()
      ),

      MonadStateLaws.laws(
        WriterT.monadState<String, StateTPartialOf<Int, ForId>, Int>(StateT.monadState(Id.monad()), String.monoid()),
        WriterT.genK(StateT.genK(Id.genK(), Gen.int()), Gen.string()), Gen.int(),
        WriterT.eqK(StateT.eqK(Id.eqK(), Int.eq(), 1), String.eq()), Int.eq()
      )
    )
  }
}
