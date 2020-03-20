package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.either
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import io.kotlintest.properties.Gen

fun <L, F> EitherT.Companion.genK(genkF: GenK<F>, genL: Gen<L>) =
  object : GenK<EitherTPartialOf<L, F>> {
    override fun <R> genK(gen: Gen<R>): Gen<Kind<EitherTPartialOf<L, F>, R>> =
      genkF.genK(Gen.either(genL, gen)).map { EitherT(it) }
  }
