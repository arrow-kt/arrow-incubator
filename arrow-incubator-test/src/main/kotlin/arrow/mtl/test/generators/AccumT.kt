package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.core.toT
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.typeclasses.Monad
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map

fun <S, F, A> AccumT.Companion.gen(MF: Monad<F>, gen: Arb<Kind<F, A>>, genS: Arb<S>) =
  Arb.bind(gen, genS) { fa, s -> AccumT<S, F, A> { MF.run { fa.map { s toT it } } } }

fun <S, F> AccumT.Companion.genK(genkF: GenK<F>, genS: Arb<S>) =
  object : GenK<AccumTPartialOf<S, F>> {
    override fun <A> genK(gen: Arb<A>): Arb<Kind<AccumTPartialOf<S, F>, A>> =
      genkF.genK(Arb.tuple2(genS, gen)).map {
        AccumT { _: S -> it }
      }
  }
