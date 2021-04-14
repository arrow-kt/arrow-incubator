package arrow.free

import arrow.Kind
import arrow.Kind2
import arrow.core.ForOption
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.option.functor.functor
import arrow.core.identity
import arrow.free.extensions.coyoneda.functor.functor
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.laws.FunctorLaws
import arrow.core.test.laws.internal.Id
import arrow.core.test.laws.internal.fix
import arrow.core.test.laws.internal.idApplicative
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe

class CoyonedaTest : UnitSpec() {

  private val EQK = object : EqK<CoyonedaPartialOf<Id.Companion, Int>> {
    override fun <A> Kind<CoyonedaPartialOf<Id.Companion, Int>, A>.eqK(other: Kind<CoyonedaPartialOf<Id.Companion, Int>, A>, EQ: Eq<A>): Boolean {
      return this.fix().lower(idApplicative) == other.fix().lower(idApplicative)
    }
  }

  private fun genk() = object : GenK<Kind2<ForCoyoneda, Id.Companion, Int>> {
    override fun <A> genK(gen: Gen<A>): Gen<Kind<Kind2<ForCoyoneda, Id.Companion, Int>, A>> =
      gen.map {
        Coyoneda(Id(0)) {
          it
        }
      } as Gen<Kind<Kind2<ForCoyoneda, Id.Companion, Int>, A>>
  }

  init {

    testLaws(FunctorLaws.laws(Coyoneda.functor(), genk(), EQK))

    "map should be stack-safe" {
      val loops = 10000

      tailrec fun loop(n: Int, acc: Coyoneda<ForOption, Int, Int>): Coyoneda<ForOption, Int, Int> =
        if (n <= 0) acc
        else loop(n - 1, acc.map { it + 1 })

      val result = loop(loops, arrow.free.Coyoneda(Some(0), ::identity)).lower(Option.functor())
      val expected = Some(loops)

      expected shouldBe result
    }

    "toYoneda should convert to an equivalent Yoneda" {
      forAll { x: Int ->
        val op = Coyoneda(Id(x), Int::toString)
        val toYoneda = op.toYoneda(idApplicative).lower().fix()
        val expected = Yoneda(Id(x.toString()), idApplicative).lower().fix()

        expected == toYoneda
      }
    }
  }
}
