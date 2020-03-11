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
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.test.generators.option
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.FunctorFilterLaws
import arrow.core.test.laws.MonoidKLaws
import arrow.core.test.laws.SemigroupKLaws
import arrow.core.test.laws.TraverseFilterLaws
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.EitherT
import arrow.mtl.ForOptionT
import arrow.mtl.OptionT
import arrow.mtl.OptionTPartialOf
import arrow.mtl.eq.EqTrans
import arrow.mtl.extensions.ComposedFunctorFilter
import arrow.mtl.extensions.core.monadBaseControl
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.extensions.monadTransControl
import arrow.mtl.extensions.nested
import arrow.mtl.extensions.optiont.applicative.applicative
import arrow.mtl.extensions.optiont.divisible.divisible
import arrow.mtl.extensions.optiont.eqK.eqK
import arrow.mtl.extensions.optiont.functor.functor
import arrow.mtl.extensions.optiont.functorFilter.functorFilter
import arrow.mtl.extensions.optiont.monad.monad
import arrow.mtl.extensions.optiont.monadTrans.monadTrans
import arrow.mtl.extensions.optiont.monoidK.monoidK
import arrow.mtl.extensions.optiont.semigroupK.semigroupK
import arrow.mtl.extensions.optiont.traverseFilter.traverseFilter
import arrow.mtl.generators.GenTrans
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.generators.nested
import arrow.typeclasses.EqK
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
      ConcurrentLaws.laws(
        OptionT.concurrent(IO.concurrent()),
        OptionT.timer(IO.concurrent()),
        OptionT.functor(IO.functor()),
        OptionT.applicative(IO.monad()),
        OptionT.monad(IO.monad()),
        OptionT.genK(IO.genK()),
        ioEQK
      ),

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

      MonadTransControlLaws.laws(
        OptionT.monadTransControl(),
        object : GenTrans<ForOptionT> {
          override fun <F> liftGenK(MF: Monad<F>, genK: GenK<F>): GenK<Kind<ForOptionT, F>> =
            OptionT.genK(genK)
        },
        object : EqTrans<ForOptionT> {
          override fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<ForOptionT, F>> =
            OptionT.eqK(eqK)
        }
      ),

      MonadBaseControlLaws.laws(
        OptionT.monadBaseControl(Id.monadBaseControl()),
        OptionT.genK(Id.genK()),
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

fun <F> OptionT.Companion.genK(genkF: GenK<F>) = object : GenK<Kind<ForOptionT, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<Kind<ForOptionT, F>, A>> = genkF.genK(Gen.option(gen)).map {
    OptionT(it)
  }
}
