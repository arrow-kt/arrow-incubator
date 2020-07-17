package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

fun <S, F> StateT.Companion.genK(genkF: GenK<F>, genS: Arb<S>) = object : GenK<StateTPartialOf<S, F>> {
  override fun <A> genK(gen: Arb<A>): Arb<Kind<StateTPartialOf<S, F>, A>> =
    genkF.genK(Arb.tuple2(genS, gen)).map { state ->
      StateT { _: S -> state }
    }
}
