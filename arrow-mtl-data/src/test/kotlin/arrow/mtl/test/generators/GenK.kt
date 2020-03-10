package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.typeclasses.Nested
import arrow.mtl.typeclasses.nest
import io.kotlintest.properties.Gen

fun <L, F> EitherT.Companion.genK(genkF: GenK<F>, genL: Gen<L>) =
  object : GenK<EitherTPartialOf<L, F>> {
    override fun <R> genK(gen: Gen<R>): Gen<Kind<EitherTPartialOf<L, F>, R>> =
      genkF.genK(Gen.either(genL, gen)).map { EitherT(it) }
  }

fun IO.Companion.genK() = object : GenK<ForIO> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<ForIO, A>> =
    gen.map {
      IO.just(it)
    }
}

fun <F, G> GenK<F>.nested(GENKG: GenK<G>): GenK<Nested<F, G>> = object : GenK<Nested<F, G>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<Nested<F, G>, A>> =
    this@nested.genK(GENKG.genK(gen)).map { it.nest() }
}
