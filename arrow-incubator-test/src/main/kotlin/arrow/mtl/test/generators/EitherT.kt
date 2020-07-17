package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.either
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

fun <L, F> EitherT.Companion.genK(genkF: GenK<F>, genL: Arb<L>) =
  object : GenK<EitherTPartialOf<L, F>> {
    override fun <R> genK(gen: Arb<R>): Arb<Kind<EitherTPartialOf<L, F>, R>> =
      genkF.genK(Arb.either(genL, gen)).map { EitherT(it) }
  }
