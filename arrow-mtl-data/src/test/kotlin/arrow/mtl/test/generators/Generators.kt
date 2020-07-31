package arrow.mtl.test.generators

import arrow.core.*
import arrow.core.extensions.listk.semialign.semialign
import arrow.core.extensions.sequencek.semialign.semialign
import arrow.mtl.Can
import io.kotlintest.properties.Gen

fun <A, B> Gen.Companion.can(genA: Gen<A>, genB: Gen<B>): Gen<Can<A, B>> =
  genA.orNull().alignWith(genB.orNull()) { Can.fromNullables(it.leftOrNull(), it.orNull()) }

private fun <A, B, R> Gen<A>.alignWith(genB: Gen<B>, transform: (Ior<A, B>) -> R): Gen<R> =
  object : Gen<R> {
    override fun constants(): Iterable<R> =
      ListK.semialign().run {
        alignWith(this@alignWith.constants().toList().k(), genB.constants().toList().k(), transform)
      }.fix()

    override fun random(): Sequence<R> =
      SequenceK.semialign().run {
        alignWith(this@alignWith.random().k(), genB.random().k(), transform)
      }.fix()
  }
