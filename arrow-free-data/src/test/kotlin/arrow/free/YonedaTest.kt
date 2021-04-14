package arrow.free

import arrow.Kind
import arrow.free.extensions.yoneda.functor.functor
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

class YonedaTest : UnitSpec() {

  private val EQK = object : EqK<YonedaPartialOf<Id.Companion>> {
    override fun <A> Kind<YonedaPartialOf<Id.Companion>, A>.eqK(other: Kind<YonedaPartialOf<Id.Companion>, A>, EQ: Eq<A>): Boolean {
      return this.fix().lower() == other.fix().lower()
    }
  }

  private fun genk() = object : GenK<Kind<ForYoneda, Id.Companion>> {
    override fun <A> genK(gen: Gen<A>): Gen<Kind<Kind<ForYoneda, Id.Companion>, A>> =
      gen.map {
        Yoneda(Id(it), idApplicative)
      }
  }

  init {
    testLaws(FunctorLaws.laws(Yoneda.functor(), genk(), EQK))

    "toCoyoneda should convert to an equivalent Coyoneda" {
      forAll { x: Int ->
        val op = Yoneda(Id(x.toString()), idApplicative)
        val toYoneda = op.toCoyoneda().lower(idApplicative).fix()
        val expected = Coyoneda(Id(x), Int::toString).lower(idApplicative).fix()

        expected == toYoneda
      }
    }
  }
}
