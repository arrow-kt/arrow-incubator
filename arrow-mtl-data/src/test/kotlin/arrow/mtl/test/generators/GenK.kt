package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.mtl.Option2
import arrow.mtl.Option2PartialOf
import arrow.mtl.typeclasses.Nested
import arrow.mtl.typeclasses.nest
import io.kotlintest.properties.Gen

fun <F, G> GenK<F>.nested(GENKG: GenK<G>): GenK<Nested<F, G>> = object : GenK<Nested<F, G>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<Nested<F, G>, A>> =
    this@nested.genK(GENKG.genK(gen)).map { it.nest() }
}

fun <A> Option2.Companion.genK(kgen: Gen<A>) =
  object : GenK<Option2PartialOf<A>> {
    @Suppress("UNCHECKED_CAST")
    override fun <B> genK(gen: Gen<B>): Gen<Kind<Option2PartialOf<A>, B>> =
      Gen.option2(kgen, gen) as Gen<Kind<Option2PartialOf<A>, B>>
  }
