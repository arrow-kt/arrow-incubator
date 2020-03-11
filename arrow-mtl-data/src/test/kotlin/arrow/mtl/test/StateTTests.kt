package arrow.mtl.test

import arrow.Kind
import arrow.core.ForId
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.monadCombine.monadCombine
import arrow.core.extensions.option.semigroupK.semigroupK
import arrow.core.extensions.tuple2.eq.eq
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.test.generators.tuple2
import arrow.core.test.laws.MonadCombineLaws
import arrow.core.test.laws.SemigroupKLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.statet.async.async
import arrow.fx.test.laws.AsyncLaws
import arrow.mtl.ForStateT
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.eq.EqTrans
import arrow.mtl.extensions.core.monadBaseControl
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.extensions.monadTransControl
import arrow.mtl.extensions.statet.applicative.applicative
import arrow.mtl.extensions.statet.functor.functor
import arrow.mtl.extensions.statet.monad.monad
import arrow.mtl.extensions.statet.monadCombine.monadCombine
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.statet.semigroupK.semigroupK
import arrow.mtl.fix
import arrow.mtl.generators.GenTrans
import arrow.mtl.run
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen

class StateTTests : UnitSpec() {

  init {
    testLaws(
      MonadStateLaws.laws<StateTPartialOf<Int, ForIO>>(
        StateT.monadState(IO.monad()),
        StateT.functor(IO.functor()),
        StateT.applicative(IO.monad()),
        StateT.monad(IO.monad()),
        StateT.genK(IO.genK(), Gen.int()),
        StateT.eqK(IO.eqK(), Int.eq(), IO.monad(), 0)
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
      ),

      MonadTransControlLaws.laws(
        StateT.monadTransControl(),
        object : GenTrans<Kind<ForStateT, Int>> {
          override fun <F> liftGenK(MF: Monad<F>, genK: GenK<F>): GenK<Kind<Kind<ForStateT, Int>, F>> =
            StateT.genK(genK, Gen.int())
        },
        object : EqTrans<Kind<ForStateT, Int>> {
          override fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<Kind<ForStateT, Int>, F>> =
            StateT.eqK(eqK, Int.eq(), MF, 1)
        }
      ),

      MonadBaseControlLaws.laws<ForId, StateTPartialOf<Int, ForId>>(
        StateT.monadBaseControl(Id.monadBaseControl()),
        StateT.genK(Id.genK(), Gen.int()),
        Id.genK(),
        StateT.eqK(Id.eqK(), Int.eq(), Id.monad(), 1)
      )
    )
  }
}

internal fun <S, F> StateT.Companion.eqK(EQKF: EqK<F>, EQS: Eq<S>, M: Monad<F>, s: S) = object : EqK<StateTPartialOf<S, F>> {
  override fun <A> Kind<StateTPartialOf<S, F>, A>.eqK(other: Kind<StateTPartialOf<S, F>, A>, EQ: Eq<A>): Boolean =
    (this.fix() to other.fix()).let {
      val ls = it.first.run(s)
      val rs = it.second.run(s)

      EQKF.liftEq(Tuple2.eq(EQS, EQ)).run {
        ls.eqv(rs)
      }
    }
}

internal fun <S, F> StateT.Companion.genK(genkF: GenK<F>, genS: Gen<S>) = object : GenK<StateTPartialOf<S, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<StateTPartialOf<S, F>, A>> =
    genkF.genK(Gen.tuple2(genS, gen)).map { state ->
      StateT { _: S -> state }
    }
}
