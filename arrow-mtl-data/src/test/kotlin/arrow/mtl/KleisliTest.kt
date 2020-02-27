package arrow.mtl

import arrow.Kind
import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.ForConst
import arrow.core.ForId
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.Option
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.eqK.eqK
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.mtl.extensions.kleisli.alternative.alternative
import arrow.mtl.extensions.kleisli.applicative.applicative
import arrow.mtl.extensions.kleisli.divisible.divisible
import arrow.mtl.extensions.kleisli.functor.functor
import arrow.mtl.extensions.kleisli.monad.monad
import arrow.test.UnitSpec
import arrow.test.generators.GenK
import arrow.test.generators.genK
import arrow.test.laws.AlternativeLaws
import arrow.test.laws.ConcurrentLaws
import arrow.test.laws.DivisibleLaws
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe

class KleisliTest : UnitSpec() {

  init {
    fun <D, F> genK(genkF: GenK<F>) = object : GenK<KleisliPartialOf<D, F>> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<KleisliPartialOf<D, F>, A>> = genkF.genK(gen).map { k ->
        Kleisli { _: D -> k }
      }
    }

    fun <D, F> Kleisli.Companion.eqK(EQKF: EqK<F>, d: D) = object : EqK<KleisliPartialOf<D, F>> {
      override fun <A> Kind<KleisliPartialOf<D, F>, A>.eqK(other: Kind<KleisliPartialOf<D, F>, A>, EQ: Eq<A>): Boolean =
        (this.fix() to other.fix()).let {
          val ls = it.first.run(d)
          val rs = it.second.run(d)

          EQKF.liftEq(EQ).run {
            ls.eqv(rs)
          }
        }
    }

    val optionEQK = Kleisli.eqK(Option.eqK(), 0)

    val ioEQK: EqK<Kind<Kind<ForKleisli, Int>, ForIO>> = Kleisli.eqK(IO.eqK(), 1)

    val constEQK: EqK<Kind<Kind<ForKleisli, Int>, Kind<ForConst, Int>>> = Kleisli.eqK(Const.eqK(Int.eq()), 1)

    testLaws(
      AlternativeLaws.laws(
        Kleisli.alternative<Int, ForOption>(Option.alternative()),
        genK<Int, ForOption>(Option.genK()),
        optionEQK
      ),
      ConcurrentLaws.laws<KleisliPartialOf<Int, ForIO>>(
        Kleisli.concurrent(IO.concurrent()),
        Kleisli.timer(IO.concurrent()),
        Kleisli.functor(IO.functor()),
        Kleisli.applicative(IO.applicative()),
        Kleisli.monad(IO.monad()),
        genK(IO.genK()),
        ioEQK
      ),
      DivisibleLaws.laws(
        Kleisli.divisible<Int, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        genK<Int, ConstPartialOf<Int>>(Const.genK(Gen.int())),
        constEQK
      )
    )

    "andThen should continue sequence" {
      val kleisli: Kleisli<Int, ForId, Int> = Kleisli { a: Int -> Id(a) }

      kleisli.andThen(Id.monad(), Id(3)).run(0) shouldBe Id(3)

      kleisli.andThen(Id.monad()) { b -> Id(b + 1) }.run(0) shouldBe Id(1)
    }
  }
}
