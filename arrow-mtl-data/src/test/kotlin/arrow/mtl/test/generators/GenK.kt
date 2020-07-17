package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.mtl.typeclasses.Nested
import arrow.mtl.typeclasses.nest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

fun <F, G> GenK<F>.nested(GENKG: GenK<G>): GenK<Nested<F, G>> = object : GenK<Nested<F, G>> {
  override fun <A> genK(gen: Arb<A>): Arb<Kind<Nested<F, G>, A>> =
    this@nested.genK(GENKG.genK(gen)).map { it.nest() }
}
