package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

fun <W, F> WriterT.Companion.genK(
  GENKF: GenK<F>,
  GENW: Arb<W>
) = object : GenK<WriterTPartialOf<W, F>> {
  override fun <A> genK(gen: Arb<A>): Arb<Kind<WriterTPartialOf<W, F>, A>> =
    GENKF.genK(Arb.tuple2(GENW, gen)).map(::WriterT)
}
