package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ForNonEmptyList
import arrow.core.Id
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.monoid
import arrow.core.extensions.nonemptylist.eqK.eqK
import arrow.core.extensions.nonemptylist.monad.monad
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.traverseFilter.traverseFilter
import arrow.core.test.UnitSpec
import arrow.core.test.generators.genK
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.FunctorFilterLaws
import arrow.core.test.laws.MonoidKLaws
import arrow.core.test.laws.SemigroupKLaws
import arrow.core.test.laws.TraverseFilterLaws
import arrow.fx.IO
import arrow.fx.test.eq.eqK
import arrow.mtl.EitherT
import arrow.mtl.OptionT
import arrow.mtl.OptionTPartialOf
import arrow.mtl.extensions.ComposedFunctorFilter
import arrow.mtl.extensions.nested
import arrow.mtl.extensions.optiont.applicative.applicative
import arrow.mtl.extensions.optiont.divisible.divisible
import arrow.mtl.extensions.optiont.eqK.eqK
import arrow.mtl.extensions.optiont.functorFilter.functorFilter
import arrow.mtl.extensions.optiont.monad.monad
import arrow.mtl.extensions.optiont.monadTrans.monadTrans
import arrow.mtl.extensions.optiont.monoidK.monoidK
import arrow.mtl.extensions.optiont.semigroupK.semigroupK
import arrow.mtl.extensions.optiont.traverseFilter.traverseFilter
import arrow.mtl.test.generators.genK
import arrow.mtl.test.generators.nested
import arrow.mtl.test.laws.MonadTransLaws
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

typealias OptionTNel = Kind<OptionTPartialOf<ForNonEmptyList>, Int>

class OptionTTest : UnitSpec() {

  val NELM: Monad<ForNonEmptyList> = NonEmptyList.monad()

  val ioEQK = OptionT.eqK(IO.eqK())

  init {

    val nestedEQK = OptionT.eqK(Id.eqK()).nested(OptionT.eqK(NonEmptyList.eqK()))

    testLaws(
      // ConcurrentLaws.laws(
      //   OptionT.concurrent(IO.concurrent()),
      //   OptionT.timer(IO.concurrent()),
      //   OptionT.functor(IO.functor()),
      //   OptionT.applicative(IO.monad()),
      //   OptionT.monad(IO.monad()),
      //   OptionT.genK(IO.genK()),
      //   ioEQK
      // ),

      SemigroupKLaws.laws(
        OptionT.semigroupK(Option.monad()),
        OptionT.genK(Option.genK()),
        OptionT.eqK(Option.eqK())),

      FunctorFilterLaws.laws(
        ComposedFunctorFilter(OptionT.functorFilter(Id.monad()),
          OptionT.functorFilter(NonEmptyList.monad())),
        OptionT.genK(Id.genK()).nested(OptionT.genK(NonEmptyList.genK())),
        nestedEQK),

      MonoidKLaws.laws(
        OptionT.monoidK(Option.monad()),
        OptionT.genK(Option.genK()),
        OptionT.eqK(Option.eqK())),

      FunctorFilterLaws.laws(
        OptionT.functorFilter(Option.monad()),
        OptionT.genK(Option.genK()),
        OptionT.eqK(Option.eqK())),

      TraverseFilterLaws.laws(
        OptionT.traverseFilter(Option.traverseFilter()),
        OptionT.applicative(Option.monad()),
        OptionT.genK(Option.genK()),
        OptionT.eqK(Option.eqK())
      ),

      DivisibleLaws.laws(
        OptionT.divisible(
          Const.divisible(Int.monoid())
        ),
        OptionT.genK(Const.genK(Gen.int())),
        OptionT.eqK(Const.eqK(Int.eq()))
      ),

      MonadTransLaws.laws(
        OptionT.monadTrans(),
        Id.monad(),
        OptionT.monad(Id.monad()),
        Id.genK(),
        OptionT.eqK(Id.eqK())
      )
    )

    "toLeft for Some should build a correct EitherT" {
      forAll { a: Int, b: String ->
        OptionT.fromOption(NELM, Some(a))
          .toLeft(NELM) { b } == EitherT.left<Int, ForNonEmptyList, String>(NELM, a)
      }
    }

    "toLeft for None should build a correct EitherT" {
      forAll { b: String ->
        OptionT.fromOption<ForNonEmptyList, Int>(NELM, None).toLeft(NELM) { b } == EitherT.right<Int, ForNonEmptyList, String>(NELM, b)
      }
    }

    "toRight for Some should build a correct EitherT" {
      forAll { a: Int, b: String ->
        OptionT.fromOption(NELM, Some(b))
          .toRight(NELM) { a } == EitherT.right<Int, ForNonEmptyList, String>(NELM, b)
      }
    }

    "toRight for None should build a correct EitherT" {
      forAll { a: Int ->
        OptionT.fromOption<ForNonEmptyList, String>(NELM, None).toRight(NELM) { a } == EitherT.left<Int, ForNonEmptyList, String>(NELM, a)
      }
    }
  }
}
