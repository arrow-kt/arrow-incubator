package arrow.mtl.test.generators

import arrow.Kind2
import arrow.core.test.generators.GenK2
import arrow.mtl.Can
import arrow.mtl.ForCan
import io.kotlintest.properties.Gen

fun Can.Companion.genK2() =
  object : GenK2<ForCan> {
    @Suppress("UNCHECKED_CAST")
    override fun <A, B> genK(genA: Gen<A>, genB: Gen<B>): Gen<Kind2<ForCan, A, B>> =
      Gen.can(genA, genB) as Gen<Kind2<ForCan, A, B>>
  }
