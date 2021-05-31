package arrow.mtl.test.generators

import arrow.Kind
import arrow.mtl.Can
import arrow.mtl.CanPartialOf
import arrow.core.test.generators.GenK
import arrow.mtl.typeclasses.Nested
import arrow.mtl.typeclasses.nest
import io.kotlintest.properties.Gen

fun <F, G> GenK<F>.nested(GENKG: GenK<G>): GenK<Nested<F, G>> = object : GenK<Nested<F, G>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<Nested<F, G>, A>> =
    this@nested.genK(GENKG.genK(gen)).map { it.nest() }
}

fun <A> Can.Companion.genK(kgen: Gen<A>) =
  object : GenK<CanPartialOf<A>> {
    @Suppress("UNCHECKED_CAST")
    override fun <B> genK(gen: Gen<B>): Gen<Kind<CanPartialOf<A>, B>> =
      Gen.can(kgen, gen) as Gen<Kind<CanPartialOf<A>, B>>
  }
