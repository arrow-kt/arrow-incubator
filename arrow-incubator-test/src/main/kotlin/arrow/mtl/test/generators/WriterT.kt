package arrow.mtl.test.generators

import arrow.Kind
import arrow.core.test.generators.GenK
import arrow.core.test.generators.tuple2
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import io.kotlintest.properties.Gen

fun <W, F> WriterT.Companion.genK(
  GENKF: GenK<F>,
  GENW: Gen<W>
) = object : GenK<WriterTPartialOf<W, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<WriterTPartialOf<W, F>, A>> =
    GENKF.genK(Gen.tuple2(GENW, gen)).map(::WriterT)
}
