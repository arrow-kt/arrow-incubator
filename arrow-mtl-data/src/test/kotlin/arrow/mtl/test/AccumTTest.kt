package arrow.mtl.test

import arrow.Kind
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.ForId
import arrow.core.Id
import arrow.core.Option
import arrow.core.extensions.either.eqK.eqK
import arrow.core.extensions.either.functor.functor
import arrow.core.extensions.either.monad.monad
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.monad.monad
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.generators.throwable
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.MonadErrorLaws
import arrow.core.test.laws.MonadPlusLaws
import arrow.core.toT
import arrow.fx.IO
import arrow.fx.extensions.monadIO
import arrow.fx.mtl.monadIO
import arrow.fx.unsafeRunSync
import arrow.fx.fix
import arrow.fx.test.laws.equalUnderTheLaw
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.mtl.Kleisli
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.extensions.accumt.alternative.alternative
import arrow.mtl.extensions.accumt.functor.functor
import arrow.mtl.extensions.accumt.monad.monad
import arrow.mtl.extensions.accumt.monadError.monadError
import arrow.mtl.extensions.accumt.monadPlus.monadPlus
import arrow.mtl.extensions.accumt.monadReader.monadReader
import arrow.mtl.extensions.accumt.monadState.monadState
import arrow.mtl.extensions.accumt.monadTrans.monadTrans
import arrow.mtl.extensions.accumt.monadWriter.monadWriter
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.fix
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadTransLaws
import arrow.mtl.test.laws.MonadWriterLaws
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad
import arrow.typeclasses.Monoid
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe

class AccumTTest : UnitSpec() {
  init {

    testLaws(

      MonadTransLaws.laws(
        AccumT.monadTrans(String.monoid()),
        Id.monad(),
        AccumT.monad(String.monoid(), Id.monad()),
        Id.genK(),
        AccumT.eqK(Id.eqK(), String.eq(), "hello")
      ),

      AlternativeLaws.laws(
        AccumT.alternative(Option.alternative(), Option.monad(), Int.monoid()),
        AccumT.genK(Option.genK(), Gen.int()),
        AccumT.eqK(Option.eqK(), Int.eq(), 10)
      ),

      MonadErrorLaws.laws<AccumTPartialOf<Int, EitherPartialOf<Throwable>>>(
        AccumT.monadError(Int.monoid(), Either.monadError()),
        AccumT.functor(Either.functor()),
        AccumT.monad(Int.monoid(), Either.monad()),
        AccumT.monad(Int.monoid(), Either.monad()),
        AccumT.genK(Either.genK(Gen.throwable()), Gen.int()),
        AccumT.eqK(Either.eqK(Eq.any()) as EqK<EitherPartialOf<Throwable>>, Int.eq(), 10)
      ),

      MonadStateLaws.laws(
        AccumT.monadState<Int, Int, StateTPartialOf<Int, ForId>>(StateT.monadState(Id.monad()), Int.monoid()),
        AccumT.genK(StateT.genK(Id.genK(), Gen.int()), Gen.int()),
        Gen.int(),
        AccumT.eqK(StateT.eqK(Id.eqK(), Int.eq(), 1), Int.eq(), 1),
        Int.eq()
      ),

      MonadWriterLaws.laws(
        AccumT.monadWriter(WriterT.monadWriter(Id.monad(), String.monoid()), String.monoid()),
        String.monoid(), Gen.string(),
        AccumT.genK(WriterT.genK(Id.genK(), Gen.string()), Gen.string()),
        AccumT.eqK(WriterT.eqK(Id.eqK(), String.eq()), String.eq(), ""),
        String.eq()
      ),

      MonadReaderLaws.laws(
        AccumT.monadReader(Kleisli.monadReader<String, ForId>(Id.monad()), String.monoid()),
        AccumT.genK(Kleisli.genK<String, ForId>(Id.genK()), Gen.string()),
        Gen.string(), AccumT.eqK(Kleisli.eqK(Id.eqK(), ""), String.eq(), ""), String.eq()
      ),

      MonadPlusLaws.laws(
        AccumT.monadPlus(Option.monad(), Int.monoid(), Option.alternative()),
        AccumT.genK(Option.genK(), Gen.int()),
        AccumT.eqK(Option.eqK(), Int.eq(), 10)
      )
    )

    "AccumT: flatMap combines State" {
      flatMapCombinesState(
        String.monoid(),
        Id.monad(),
        Gen.string(),
        Gen.bool(),
        Id.eqK().liftEq(String.eq())
      )
    }

    "AccumT: ap combines State" {
      apCombinesState(
        String.monoid(),
        Id.monad(),
        Gen.string(),
        Gen.bool(),
        Id.eqK().liftEq(String.eq())
      )
    }

    "AccumT: monadIO" {
      val accumT = AccumT.monadIO(IO.monadIO(), String.monoid()).run {
        IO.just(1).liftIO().fix()
      }

      val ls = accumT.runAccumT("1").fix().unsafeRunSync()

      ls shouldBe ("" toT 1)
    }
  }
}

private fun <S, F, A> flatMapCombinesState(
  MS: Monoid<S>,
  MF: Monad<F>,
  GENS: Gen<S>,
  GENA: Gen<A>,
  eq: Eq<Kind<F, S>>
): Unit =
  forAll(GENS, GENS, GENS, GENA) { g1, g2, g3, a ->

    val accumT = AccumT.add(MF, g1)

    val ls = accumT.flatMap(MS, MF) {
      AccumT.add(MF, g2)
    }.execAccumT(MF, g3)

    val rs = MF.just(MS.run { g1.combine(g2) })

    ls.equalUnderTheLaw(rs, eq)
  }

private fun <S, F, A> apCombinesState(
  MS: Monoid<S>,
  MF: Monad<F>,
  GENS: Gen<S>,
  GENA: Gen<A>,
  eq: Eq<Kind<F, S>>
): Unit =
  forAll(GENS, GENS, GENS, GENA) { s1, s2, s3, a ->

    val accumT = AccumT { _: S ->
      MF.just(s1 toT a)
    }

    val mf = AccumT { _: S ->
      MF.just(s2 toT { a: A -> a })
    }

    val ls = accumT.ap(MS, MF, mf).execAccumT(MF, s3)
    val rs = MF.just(MS.run { s1.combine(s2) })

    ls.equalUnderTheLaw(rs, eq)
  }
