package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.Either
import arrow.core.ForConst
import arrow.core.ForId
import arrow.core.Id
import arrow.core.Left
import arrow.core.Option
import arrow.core.Right
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.id.traverse.traverse
import arrow.core.extensions.monoid
import arrow.core.extensions.option.functor.functor
import arrow.fx.IO
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.test.generators.throwable
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.MonadErrorLaws
import arrow.core.test.laws.TraverseLaws
import arrow.fx.ForIO
import arrow.fx.test.eq.eqK
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.eq.throwableEq
import arrow.fx.test.generators.genK
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.ForEitherT
import arrow.mtl.eq.EqTrans
import arrow.mtl.extensions.core.monadBaseControl
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import arrow.mtl.extensions.eithert.alternative.alternative
import arrow.mtl.extensions.eithert.applicative.applicative
import arrow.mtl.extensions.eithert.apply.apply
import arrow.mtl.extensions.eithert.divisible.divisible
import arrow.mtl.extensions.eithert.eqK.eqK
import arrow.mtl.extensions.eithert.functor.functor
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.extensions.eithert.monadError.monadError
import arrow.mtl.extensions.eithert.monadReader.monadReader
import arrow.mtl.extensions.eithert.monadState.monadState
import arrow.mtl.extensions.eithert.monadWriter.monadWriter
import arrow.mtl.extensions.eithert.traverse.traverse
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.extensions.monadTransControl
import arrow.mtl.generators.GenTrans
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadWriterLaws
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class EitherTTest : UnitSpec() {

  init {
    val idEQK: EqK<Kind<Kind<ForEitherT, Int>, ForId>> = EitherT.eqK(Id.eqK(), Int.eq())

    val ioEQK: EqK<EitherTPartialOf<String, ForIO>> = EitherT.eqK(IO.eqK(), Eq.any())

    val constEQK: EqK<Kind<Kind<ForEitherT, Int>, Kind<ForConst, Int>>> = EitherT.eqK(Const.eqK(Int.eq()), Int.eq())

    testLaws(
      MonadTransControlLaws.laws(
        EitherT.monadTransControl(),
        object : GenTrans<Kind<ForEitherT, String>> {
          override fun <F> liftGenK(MF: Monad<F>, genK: GenK<F>): GenK<Kind<Kind<ForEitherT, String>, F>> =
            EitherT.genK(genK, Gen.string())
        },
        object : EqTrans<Kind<ForEitherT, String>> {
          override fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<Kind<ForEitherT, String>, F>> =
            EitherT.eqK(eqK, String.eq())
        }
      ),

      MonadBaseControlLaws.laws<ForId, EitherTPartialOf<String, ForId>>(
        EitherT.monadBaseControl(Id.monadBaseControl()),
        EitherT.genK(Id.genK(), Gen.string()),
        Id.genK(),
        EitherT.eqK(Id.eqK(), String.eq())
      ),

      DivisibleLaws.laws(
        EitherT.divisible<Int, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        EitherT.genK(Const.genK(Gen.int()), Gen.int()),
        constEQK
      ),

      AlternativeLaws.laws(
        EitherT.alternative(Id.monad(), Int.monoid()),
        EitherT.genK(Id.genK(), Gen.int()),
        idEQK
      ),

      ConcurrentLaws.laws<EitherTPartialOf<String, ForIO>>(
        EitherT.concurrent(IO.concurrent()),
        EitherT.timer(IO.concurrent()),
        EitherT.functor(IO.functor()),
        EitherT.applicative(IO.monad()),
        EitherT.monad(IO.monad()),
        EitherT.genK(IO.genK(), Gen.string()),
        ioEQK
      ),

      TraverseLaws.laws(EitherT.traverse<Int, ForId>(Id.traverse()),
        EitherT.genK(Id.genK(), Gen.int()),
        idEQK
      ),

      MonadErrorLaws.laws<EitherTPartialOf<Throwable, ForId>>(
        EitherT.monadError(Id.monad()),
        EitherT.functor(Id.monad()),
        EitherT.apply(Id.monad()),
        EitherT.monad(Id.monad()),
        EitherT.genK(Id.genK(), Gen.throwable()),
        EitherT.eqK(Id.eqK(), throwableEq())
      ),

      MonadReaderLaws.laws(
        EitherT.monadReader<String, KleisliPartialOf<String, ForId>, String>(Kleisli.monadReader(Id.monad())),
        EitherT.genK(Kleisli.genK<String, ForId>(Id.genK()), Gen.string()),
        Gen.string(), EitherT.eqK(Kleisli.eqK(Id.eqK(), "H"), String.eq()), String.eq()
      ),

      MonadWriterLaws.laws(
        EitherT.monadWriter<String, WriterTPartialOf<String, ForId>, String>(WriterT.monadWriter(Id.monad(), String.monoid())),
        String.monoid(), Gen.string(), EitherT.genK(WriterT.genK(Id.genK(), Gen.string()), Gen.string()),
        EitherT.eqK(WriterT.eqK(Id.eqK(), String.eq()), String.eq()), String.eq()
      ),

      MonadStateLaws.laws(
        EitherT.monadState<String, StateTPartialOf<Int, ForId>, Int>(StateT.monadState(Id.monad())),
        EitherT.genK(StateT.genK(Id.genK(), Gen.int()), Gen.string()), Gen.int(),
        EitherT.eqK(StateT.eqK(Id.eqK(), Int.eq(), 1), String.eq()), Int.eq()
      )
    )

    "mapLeft should alter left instance only" {
      forAll { i: Int, j: Int ->
        val left: Either<Int, Int> = Left(i)
        val right: Either<Int, Int> = Right(j)
        EitherT(Option(left)).mapLeft(Option.functor()) { it + 1 } == EitherT(Option(Left(i + 1))) &&
          EitherT(Option(right)).mapLeft(Option.functor()) { it + 1 } == EitherT(Option(right)) &&
          EitherT(Option.empty<Either<Int, Int>>()).mapLeft(Option.functor()) { it + 1 } == EitherT(Option.empty<Either<Int, Int>>())
      }
    }
  }
}
