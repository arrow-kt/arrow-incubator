package arrow.smash.generators

import arrow.core.Option
import arrow.core.SequenceK
import arrow.core.extensions.sequencek.apply.apply
import arrow.core.extensions.sequencek.monad.map
import arrow.core.k
import arrow.core.test.generators.option
import arrow.smash.Can
import io.kotlintest.properties.Gen


fun <A, B> Gen.Companion.can(genA: Gen<A>, genB: Gen<B>): Gen<Can<A, B>> =
  object : Gen<Can<A, B>> {
    override fun constants(): Iterable<Can<A, B>> =
      (genA.orNull().constants().asSequence().k() to genB.orNull().constants().asSequence().k()).let { (ls, rs) ->
        SequenceK.apply().run { ls.product(rs) }.map {
          Can.fromOptions(Option.fromNullable(it.a), Option.fromNullable(it.b))
        }.asIterable()
      }

    override fun random(): Sequence<Can<A, B>> =
      (Gen.option(genA).random() to Gen.option(genB).random()).let { (ls, rs) ->
        ls.zip(rs).map {
          Can.fromOptions(it.first, it.second)
        }
      }
  }
