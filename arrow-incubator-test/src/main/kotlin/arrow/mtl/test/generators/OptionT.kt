package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.option
import arrow.mtl.ForOptionT
import arrow.mtl.OptionT
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

fun <F> OptionT.Companion.genK(genkF: GenK<F>) = object : GenK<Kind<ForOptionT, F>> {
  override fun <A> genK(gen: Arb<A>): Arb<Kind<Kind<ForOptionT, F>, A>> = genkF.genK(Arb.option(gen)).map {
    OptionT(it)
  }
}
