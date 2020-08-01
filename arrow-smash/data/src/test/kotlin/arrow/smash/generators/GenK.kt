package arrow.smash.generators

import arrow.smash.Can
import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.smash.CanPartialOf
import io.kotlintest.properties.Gen

fun <A> Can.Companion.genK(kgen: Gen<A>) =
  object : GenK<CanPartialOf<A>> {
    @Suppress("UNCHECKED_CAST")
    override fun <B> genK(gen: Gen<B>): Gen<Kind<CanPartialOf<A>, B>> =
      Gen.can(kgen, gen) as Gen<Kind<CanPartialOf<A>, B>>
  }
