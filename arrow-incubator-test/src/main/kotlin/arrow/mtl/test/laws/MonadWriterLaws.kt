package arrow.mtl.test.laws

import arrow.Kind
import arrow.core.Tuple2
import arrow.core.extensions.eq
import arrow.core.extensions.tuple2.eq.eq
import arrow.mtl.typeclasses.MonadWriter
import arrow.core.test.generators.GenK
import arrow.core.test.laws.Law
import arrow.core.test.laws.MonadLaws
import arrow.core.test.laws.equalUnderTheLaw
import arrow.typeclasses.Apply
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Functor
import arrow.typeclasses.Monoid
import arrow.typeclasses.Selective
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll

object MonadWriterLaws {

  private fun <F, W> monadWriterLaws(
    MW: MonadWriter<F, W>,
    MOW: Monoid<W>,
    genW: Arb<W>,
    EQK: EqK<F>,
    EQW: Eq<W>
  ): List<Law> {
    val EQ_INT = EQK.liftEq(Int.eq())
    val EQ_TUPLE = EQK.liftEq(Tuple2.eq(EQW, Int.eq()))
    val GEN_TUPLE = Arb.bind(genW, Arb.int(), ::Tuple2)

    return listOf(
      Law("Monad Writer Laws: writer just") { MW.monadWriterWriterJust(MOW, EQ_INT) },
      Law("Monad Writer Laws: tell fusion") { MW.monadWriterTellFusion(genW, MOW, EQK.liftEq(Eq.any())) },
      Law("Monad Writer Laws: listen just") { MW.monadWriterListenJust(MOW, EQ_TUPLE) },
      Law("Monad Writer Laws: listen writer") { MW.monadWriterListenWriter(GEN_TUPLE, EQ_TUPLE) })
  }

  fun <F, W> laws(
    MW: MonadWriter<F, W>,
    MOW: Monoid<W>,
    genW: Arb<W>,
    GENK: GenK<F>,
    EQK: EqK<F>,
    EQW: Eq<W>
  ): List<Law> =
    MonadLaws.laws(MW, GENK, EQK) +
      monadWriterLaws(MW, MOW, genW, EQK, EQW)

  fun <F, W> laws(
    MW: MonadWriter<F, W>,
    MOW: Monoid<W>,
    FF: Functor<F>,
    AP: Apply<F>,
    SL: Selective<F>,
    genW: Arb<W>,
    GENK: GenK<F>,
    EQK: EqK<F>,
    EQW: Eq<W>
  ): List<Law> =
    MonadLaws.laws(MW, FF, AP, SL, GENK, EQK) + monadWriterLaws(MW, MOW, genW, EQK, EQW)

  private suspend fun <F, W> MonadWriter<F, W>.monadWriterWriterJust(
    MOW: Monoid<W>,
    EQ: Eq<Kind<F, Int>>
  ) {
    forAll(Arb.int()) { a: Int ->
      writer(Tuple2(MOW.empty(), a)).equalUnderTheLaw(just(a), EQ)
    }
  }

  private suspend fun <F, W> MonadWriter<F, W>.monadWriterTellFusion(
    genW: Arb<W>,
    MOW: Monoid<W>,
    EQ: Eq<Kind<F, Unit>>
  ) {
    forAll(genW, genW) { x: W, y: W ->
      val ls = tell(x).flatMap { tell(y) }
      val rs = tell(MOW.run { x.combine(y) })

      ls.equalUnderTheLaw(rs, EQ)
    }
  }

  private suspend fun <F, W> MonadWriter<F, W>.monadWriterListenJust(
    MOW: Monoid<W>,
    EqTupleWA: Eq<Kind<F, Tuple2<W, Int>>>
  ) {
    forAll(Arb.int()) { a: Int ->
      just(a).listen().equalUnderTheLaw(just(Tuple2(MOW.empty(), a)), EqTupleWA)
    }
  }

  private suspend fun <F, W> MonadWriter<F, W>.monadWriterListenWriter(
    genTupleWA: Arb<Tuple2<W, Int>>,
    EqTupleWA: Eq<Kind<F, Tuple2<W, Int>>>
  ) {
    forAll(genTupleWA) { tupleWA: Tuple2<W, Int> ->
      writer(tupleWA).listen().equalUnderTheLaw(tell(tupleWA.a).map { tupleWA }, EqTupleWA)
    }
  }
}
