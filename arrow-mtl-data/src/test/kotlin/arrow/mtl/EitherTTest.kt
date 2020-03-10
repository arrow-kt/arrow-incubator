package arrow.mtl

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
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.generators.throwable
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.MonadErrorLaws
import arrow.core.test.laws.TraverseLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.eq.throwableEq
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.extensions.eithert.alternative.alternative
import arrow.mtl.extensions.eithert.applicative.applicative
import arrow.mtl.extensions.eithert.apply.apply
import arrow.mtl.extensions.eithert.divisible.divisible
import arrow.mtl.extensions.eithert.eqK.eqK
import arrow.mtl.extensions.eithert.functor.functor
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.extensions.eithert.monadError.monadError
import arrow.mtl.extensions.eithert.traverse.traverse
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

class EitherTTest : UnitSpec() {

  init {
    val idEQK: EqK<Kind<Kind<ForEitherT, Int>, ForId>> = EitherT.eqK(Id.eqK(), Int.eq())

    val ioEQK: EqK<Kind<Kind<ForEitherT, String>, ForIO>> = EitherT.eqK(IO.eqK(), Eq.any())

    val constEQK: EqK<Kind<Kind<ForEitherT, Int>, Kind<ForConst, Int>>> = EitherT.eqK(Const.eqK(Int.eq()), Int.eq())

    testLaws(
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
        EitherT.applicative(IO.applicative()),
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
