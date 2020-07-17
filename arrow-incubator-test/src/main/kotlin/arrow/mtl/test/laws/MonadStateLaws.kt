package arrow.mtl.test.laws

import arrow.Kind
import arrow.core.Tuple2
import arrow.core.extensions.eq
import arrow.mtl.typeclasses.MonadState
import arrow.core.test.generators.GenK
import arrow.core.test.generators.functionAToB
import arrow.core.test.generators.tuple2
import arrow.core.test.laws.Law
import arrow.core.test.laws.MonadLaws
import arrow.core.test.laws.equalUnderTheLaw
import arrow.typeclasses.Apply
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Functor
import arrow.typeclasses.Selective
import io.kotest.property.Arb
import io.kotest.property.forAll
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

object MonadStateLaws {

  private fun <F, S> monadStateLaws(
    MS: MonadState<F, S>,
    genK: GenK<F>,
    genS: Arb<S>,
    EQK: EqK<F>,
    eqS: Eq<S>
  ): List<Law> {
    return listOf(
      Law("Monad State Laws: get().followedBy(m) == m") {
        MS.monadStateGetProducesNoSideEffect(genK.genK(Arb.int()), EQK.liftEq(Int.eq()))
      },
      Law("Monad State Laws: set twice eq to set once the last element") {
        MS.monadStateSetTwice(genS, EQK.liftEq(Eq.any()))
      },
      Law("Monad State Laws: set get") { MS.monadStateSetGet(genS, EQK.liftEq(eqS)) },
      Law("Monad State Laws: get set") { MS.monadStateGetSet(EQK.liftEq(Eq.any())) },
      Law("Monad State Laws: get().flatMap { s1 -> get().flatMap { s2 -> f(s1, s2) } } == get().flatMap { s -> f(s, s) }") {
        MS.getProducesTheSameResult(
          genK.genK(Arb.int()).map { { _: S, _: S -> it } },
          EQK.liftEq(Int.eq())
        )
      },
      Law("Monad State Laws: modify derived") {
        MS.modifyDerived(
          Arb.functionAToB(genS), EQK.liftEq(Eq.any())
        )
      },
      Law("Monad State Laws: inspect derived") {
        MS.inspectDerived(
          Arb.functionAToB(Arb.int()), EQK.liftEq(Int.eq())
        )
      },
      Law("Monad State Laws: state derived") {
        MS.stateDerived(
          Arb.functionAToB(Arb.tuple2(genS, Arb.int())), EQK.liftEq(Int.eq())
        )
      }
    )
  }

  fun <F, S> laws(
    MS: MonadState<F, S>,
    GENK: GenK<F>,
    genS: Arb<S>,
    EQK: EqK<F>,
    eqS: Eq<S>
  ): List<Law> =
    MonadLaws.laws(MS, GENK, EQK) + monadStateLaws(MS, GENK, genS, EQK, eqS)

  fun <F, S> laws(
    MS: MonadState<F, S>,
    FF: Functor<F>,
    AP: Apply<F>,
    SL: Selective<F>,
    GENK: GenK<F>,
    genS: Arb<S>,
    EQK: EqK<F>,
    eqS: Eq<S>
  ): List<Law> =
    MonadLaws.laws(MS, FF, AP, SL, GENK, EQK) + monadStateLaws(MS, GENK, genS, EQK, eqS)

  private suspend fun <F, S, A> MonadState<F, S>.monadStateGetProducesNoSideEffect(
    genFA: Arb<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFA) { fa ->
      get().followedBy(fa).equalUnderTheLaw(fa, EQ)
    }
  }

  private suspend fun <F, S> MonadState<F, S>.monadStateSetTwice(genS: Arb<S>, EQ: Eq<Kind<F, Unit>>) {
    forAll(genS, genS) { s, t ->
      set(s).followedBy(set(t)).equalUnderTheLaw(set(t), EQ)
    }
  }

  private suspend fun <F, S> MonadState<F, S>.monadStateSetGet(genS: Arb<S>, EQ: Eq<Kind<F, S>>) {
    forAll(genS) { s ->
      set(s).followedBy(get()).equalUnderTheLaw(set(s).mapConst(s), EQ)
    }
  }

  private suspend fun <F, S> MonadState<F, S>.monadStateGetSet(EQ: Eq<Kind<F, Unit>>) {
    get().flatMap { set(it) }.equalUnderTheLaw(just(Unit), EQ)
  }

  private suspend fun <F, S, A> MonadState<F, S>.getProducesTheSameResult(
    genFun: Arb<(S, S) -> Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { f ->
      get().flatMap { s1 -> get().flatMap { s2 -> f(s1, s2) } }
        .equalUnderTheLaw(get().flatMap { s -> f(s, s) }, EQ)
    }
  }

  private suspend fun <F, S> MonadState<F, S>.modifyDerived(
    genFun: Arb<(S) -> S>,
    EQ: Eq<Kind<F, Unit>>
  ) {
    forAll(genFun) { f ->
      modify(f).equalUnderTheLaw(get().flatMap { set(f(it)) }, EQ)
    }
  }

  private suspend fun <F, S, A> MonadState<F, S>.inspectDerived(
    genFun: Arb<(S) -> A>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { f ->
      inspect(f).equalUnderTheLaw(get().map(f), EQ)
    }
  }

  private suspend fun <F, S, A> MonadState<F, S>.stateDerived(
    genFun: Arb<(S) -> Tuple2<S, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { f ->
      state(f).equalUnderTheLaw(get().flatMap { s ->
        val (newS, res) = f(s)
        set(newS).mapConst(res)
      }, EQ)
    }
  }
}
