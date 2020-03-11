package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import io.kotlintest.properties.Gen

fun <D, F> Kleisli.Companion.genK(genkF: GenK<F>) = object : GenK<KleisliPartialOf<D, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<KleisliPartialOf<D, F>, A>> = genkF.genK(gen).map { k ->
    Kleisli { _: D -> k }
  }
}
