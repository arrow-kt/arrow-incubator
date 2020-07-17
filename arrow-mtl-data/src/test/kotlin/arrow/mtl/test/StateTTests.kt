package arrow.mtl.test

import arrow.core.ForId
import arrow.core.ForListK
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.listk.eqK.eqK
import arrow.core.extensions.listk.monadLogic.monadLogic
import arrow.core.extensions.monoid
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.monadCombine.monadCombine
import arrow.core.extensions.option.semigroupK.semigroupK
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.MonadCombineLaws
import arrow.core.test.laws.MonadLogicLaws
import arrow.core.test.laws.SemigroupKLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.statet.async.async
import arrow.fx.test.eq.eqK
import arrow.fx.test.generators.genK
import arrow.fx.test.laws.AsyncLaws
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.statet.applicative.applicative
import arrow.mtl.extensions.statet.functor.functor
import arrow.mtl.extensions.statet.monad.monad
import arrow.mtl.extensions.statet.monadCombine.monadCombine
import arrow.mtl.extensions.statet.monadLogic.monadLogic
import arrow.mtl.extensions.statet.monadReader.monadReader
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.statet.monadWriter.monadWriter
import arrow.mtl.extensions.statet.semigroupK.semigroupK
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadWriterLaws
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

class StateTTests : UnitSpec() {

  init {
    testLaws(
      AsyncLaws.laws<StateTPartialOf<Int, ForIO>>(
        StateT.async(IO.async()),
        StateT.functor(IO.functor()),
        StateT.applicative(IO.monad()),
        StateT.monad(IO.monad()),
        StateT.genK(IO.genK(), Arb.int()),
        StateT.eqK(IO.eqK(), Int.eq(), 0)
      ),

      SemigroupKLaws.laws(
        StateT.semigroupK<Int, ForOption>(Option.semigroupK()),
        StateT.genK(Option.genK(), Arb.int()),
        StateT.eqK(Option.eqK(), Int.eq(), 0)
      ),

      MonadCombineLaws.laws<StateTPartialOf<Int, ForOption>>(
        StateT.monadCombine(Option.monadCombine()),
        StateT.functor(Option.functor()),
        StateT.applicative(Option.monad()),
        StateT.monad(Option.monad()),
        StateT.genK(Option.genK(), Arb.int()),
        StateT.eqK(Option.eqK(), Int.eq(), 0)
      ),

      MonadLogicLaws.laws(
        StateT.monadLogic<Int, ForListK>(ListK.monadLogic()),
        StateT.genK(ListK.genK(withMaxSize = 20), Arb.int()),
        StateT.eqK(ListK.eqK(), Int.eq(), 1), 50
      ),

      MonadStateLaws.laws<StateTPartialOf<Int, ForIO>, Int>(
        StateT.monadState(IO.monad()),
        StateT.genK(IO.genK(), Arb.int()),
        Arb.int(),
        StateT.eqK(IO.eqK(), Int.eq(), 0),
        Int.eq()
      ),

      MonadReaderLaws.laws(
        StateT.monadReader<Int, KleisliPartialOf<Int, ForId>, Int>(Kleisli.monadReader(Id.monad())),
        StateT.genK(Kleisli.genK<Int, ForId>(Id.genK()), Arb.int()), Arb.int(),
        StateT.eqK<Int, KleisliPartialOf<Int, ForId>>(Kleisli.eqK(Id.eqK(), 1), Int.eq(), 0), Int.eq()
      ),

      MonadWriterLaws.laws(
        StateT.monadWriter<Int, WriterTPartialOf<String, ForId>, String>(WriterT.monadWriter(Id.monad(), String.monoid())),
        String.monoid(), Arb.string(),
        StateT.genK(WriterT.genK(Id.genK(), Arb.string()), Arb.int()),
        StateT.eqK(WriterT.eqK(Id.eqK(), String.eq()), Int.eq(), 1), String.eq()
      )
    )
  }
}
