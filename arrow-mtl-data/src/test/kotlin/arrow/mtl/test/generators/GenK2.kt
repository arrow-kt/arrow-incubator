package arrow.mtl.test.generators

import arrow.Kind2
import arrow.core.test.generators.GenK2
import arrow.mtl.ForOption2
import arrow.mtl.Option2
import io.kotlintest.properties.Gen

@Suppress("UNCHECKED_CAST")
fun Option2.Companion.genK2() =
  object : GenK2<ForOption2> {
    override fun <A, B> genK(genA: Gen<A>, genB: Gen<B>): Gen<Kind2<ForOption2, A, B>> =
      Gen.option2(genA, genB) as Gen<Kind2<ForOption2, A, B>>
  }
