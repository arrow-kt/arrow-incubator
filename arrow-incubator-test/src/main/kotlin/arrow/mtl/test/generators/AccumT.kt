package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.core.toT
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen

fun <S, F, A> AccumT.Companion.gen(MF: Monad<F>, gen: Gen<Kind<F, A>>, genS: Gen<S>) =
  Gen.bind(gen, genS) { fa, s -> AccumT<S, F, A> { MF.run { fa.map { s toT it } } } }

fun <S, F> AccumT.Companion.genK(genkF: GenK<F>, genS: Gen<S>) =
  object : GenK<AccumTPartialOf<S, F>> {
    override fun <A> genK(gen: Gen<A>): Gen<Kind<AccumTPartialOf<S, F>, A>> =
      genkF.genK(Gen.tuple2(genS, gen)).map {
        AccumT { _: S -> it }
      }
  }
