package arrow.mtl.test

import arrow.core.ForOption
import arrow.core.Option
import arrow.core.extensions.eq
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.monadCombine.monadCombine
import arrow.core.extensions.option.semigroupK.semigroupK
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.MonadCombineLaws
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
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.extensions.statet.applicative.applicative
import arrow.mtl.extensions.statet.functor.functor
import arrow.mtl.extensions.statet.monad.monad
import arrow.mtl.extensions.statet.monadCombine.monadCombine
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.statet.semigroupK.semigroupK
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadStateLaws
import io.kotlintest.properties.Gen

class StateTTests : UnitSpec() {

  init {
    testLaws(
      MonadStateLaws.laws<StateTPartialOf<Int, ForIO>, Int>(
        StateT.monadState(IO.monad()),
        StateT.genK(IO.genK(), Gen.int()),
        Gen.int(),
        StateT.eqK(IO.eqK(), Int.eq(), IO.monad(), 0),
        Int.eq()
      ),

      AsyncLaws.laws<StateTPartialOf<Int, ForIO>>(
        StateT.async(IO.async()),
        StateT.functor(IO.functor()),
        StateT.applicative(IO.monad()),
        StateT.monad(IO.monad()),
        StateT.genK(IO.genK(), Gen.int()),
        StateT.eqK(IO.eqK(), Int.eq(), IO.monad(), 0)
      ),

      SemigroupKLaws.laws(
        StateT.semigroupK<Int, ForOption>(Option.semigroupK()),
        StateT.genK(Option.genK(), Gen.int()),
        StateT.eqK(Option.eqK(), Int.eq(), Option.monad(), 0)
      ),

      MonadCombineLaws.laws<StateTPartialOf<Int, ForOption>>(
        StateT.monadCombine(Option.monadCombine()),
        StateT.functor(Option.functor()),
        StateT.applicative(Option.monad()),
        StateT.monad(Option.monad()),
        StateT.genK(Option.genK(), Gen.int()),
        StateT.eqK(Option.eqK(), Int.eq(), Option.monad(), 0)
      )
    )
  }
}
