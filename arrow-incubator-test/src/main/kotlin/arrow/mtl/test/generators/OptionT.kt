package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.option
import arrow.mtl.ForOptionT
import arrow.mtl.OptionT
import io.kotlintest.properties.Gen

fun <F> OptionT.Companion.genK(genkF: GenK<F>) = object : GenK<Kind<ForOptionT, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<Kind<ForOptionT, F>, A>> = genkF.genK(Gen.option(gen)).map {
    OptionT(it)
  }
}
