package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ForOption
import arrow.core.ForNonEmptyList
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.monoid
import arrow.core.extensions.nonemptylist.eqK.eqK
import arrow.core.extensions.nonemptylist.monad.monad
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.traverseFilter.traverseFilter
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.FunctorFilterLaws
import arrow.core.test.laws.MonadPlusLaws
import arrow.core.test.laws.MonoidKLaws
import arrow.core.test.laws.SemigroupKLaws
import arrow.core.test.laws.TraverseFilterLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.test.eq.eqK
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.generators.genK
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.EitherT
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.OptionT
import arrow.mtl.OptionTPartialOf
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.WriterT
import arrow.mtl.extensions.ComposedFunctorFilter
import arrow.mtl.extensions.kleisli.monadReader.monadReader
import arrow.mtl.extensions.nested
import arrow.mtl.extensions.optiont.applicative.applicative
import arrow.mtl.extensions.optiont.divisible.divisible
import arrow.mtl.extensions.optiont.eqK.eqK
import arrow.mtl.extensions.optiont.functor.functor
import arrow.mtl.extensions.optiont.functorFilter.functorFilter
import arrow.mtl.extensions.optiont.monad.monad
import arrow.mtl.extensions.optiont.monadPlus.monadPlus
import arrow.mtl.extensions.optiont.monadReader.monadReader
import arrow.mtl.extensions.optiont.monadState.monadState
import arrow.mtl.extensions.optiont.monadTrans.monadTrans
import arrow.mtl.extensions.optiont.monadWriter.monadWriter
import arrow.mtl.extensions.optiont.monoidK.monoidK
import arrow.mtl.extensions.optiont.semigroupK.semigroupK
import arrow.mtl.extensions.optiont.traverseFilter.traverseFilter
import arrow.mtl.extensions.statet.monadState.monadState
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.mtl.test.generators.nested
import arrow.mtl.test.laws.MonadReaderLaws
import arrow.mtl.test.laws.MonadStateLaws
import arrow.mtl.test.laws.MonadTransLaws
import arrow.mtl.test.laws.MonadWriterLaws
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

typealias OptionTNel = Kind<OptionTPartialOf<ForNonEmptyList>, Int>

class OptionTTest : UnitSpec() {

  val NELM: Monad<ForNonEmptyList> = NonEmptyList.monad()

  val ioEQK = OptionT.eqK<ForIO>(IO.eqK())

  fun someK() = object : GenK<ForOption> {
    override fun <A> genK(gen: Gen<A>): Gen<Kind<ForOption, A>> =
      gen.map(::Some) as Gen<Kind<ForOption, A>>
  }

  init {

    val nestedEQK = OptionT.eqK(Option.eqK()).nested(OptionT.eqK(NonEmptyList.eqK()))

    testLaws(
      ConcurrentLaws.laws<OptionTPartialOf<ForIO>>(
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
        ComposedFunctorFilter(OptionT.functorFilter(Option.monad()),
          OptionT.functorFilter(NonEmptyList.monad())),
        OptionT.genK(Option.genK()).nested(OptionT.genK(NonEmptyList.genK())),
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
        Option.monad(),
        OptionT.monad(Option.monad()),
        Option.genK(),
        OptionT.eqK(Option.eqK())
      ),

      MonadPlusLaws.laws(
        OptionT.monadPlus(Option.monad()),
        OptionT.genK(someK()),
        OptionT.eqK(Option.eqK())
      ),

      MonadReaderLaws.laws(
        OptionT.monadReader<KleisliPartialOf<Int, ForOption>, Int>(Kleisli.monadReader(Option.monad())),
        OptionT.genK(Kleisli.genK<Int, ForOption>(Option.genK())), Gen.int(),
        OptionT.eqK(Kleisli.eqK(Option.eqK(), 1)), Int.eq()
      ),

      MonadWriterLaws.laws(
        OptionT.monadWriter(WriterT.monadWriter(Option.monad(), String.monoid())), String.monoid(), Gen.string(),
        OptionT.genK(WriterT.genK(Option.genK(), Gen.string())),
        OptionT.eqK(WriterT.eqK(Option.eqK(), String.eq())), String.eq()
      ),

      MonadStateLaws.laws(
        OptionT.monadState<StateTPartialOf<Int, ForOption>, Int>(StateT.monadState(Option.monad())),
        OptionT.genK(StateT.genK(Option.genK(), Gen.int())), Gen.int(),
        OptionT.eqK(StateT.eqK(Option.eqK(), Int.eq(), 1)), Int.eq()
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
