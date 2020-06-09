package arrow.mtl.test.generators

import arrow.core.Option
import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.getOrElse
import arrow.core.test.generators.option
import arrow.mtl.Option2
import io.kotlintest.properties.Gen

fun <A, B> Gen.Companion.option2(genA: Gen<A>, genB: Gen<B>): Gen<Option2<A, B>> =
  object : Gen<Option2<A, B>> {
    override fun constants(): Iterable<Option2<A, B>> =
      Gen.option(genA).constants().asSequence().zip(Gen.option(genB).constants().asSequence())
        .map {
          Option.applicative()
            .mapN(it.first, it.second) { (a, b) -> Option2(a, b) }.fix()
            .getOrElse { Option2.None }
        }.asIterable()

    override fun random(): Sequence<Option2<A, B>> =
      Gen.option(genA).random().zip(Gen.option(genB).random()).map {
        Option.applicative()
          .mapN(it.first, it.second) { (a, b) -> Option2(a, b) }.fix()
          .getOrElse { Option2.None }
      }
  }
