package arrow.mtl.test.generators

import arrow.mtl.Can
import io.kotlintest.properties.Gen

fun <A, B> Gen.Companion.can(genA: Gen<A>, genB: Gen<B>): Gen<Can<A, B>> =
  genA.zip(genB) { a, b -> Can.fromNullables(a, b) }

private fun <A, B, R> Gen<A>.zip(genB: Gen<B>, transform: (A, B) -> R): Gen<R> =
  object : Gen<R> {
    override fun constants(): Iterable<R> = this@zip.constants().zip(genB.constants(), transform)
    override fun random(): Sequence<R> = this@zip.random().zip(genB.random(), transform)
  }
