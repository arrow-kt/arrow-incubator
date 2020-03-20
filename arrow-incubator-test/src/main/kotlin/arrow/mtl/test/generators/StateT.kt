package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import io.kotlintest.properties.Gen

fun <S, F> StateT.Companion.genK(genkF: GenK<F>, genS: Gen<S>) = object : GenK<StateTPartialOf<S, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<StateTPartialOf<S, F>, A>> =
    genkF.genK(Gen.tuple2(genS, gen)).map { state ->
      StateT { _: S -> state }
    }
}
