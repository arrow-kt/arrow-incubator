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
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadStateLaws {

  private fun <F, S> monadStateLaws(
    MS: MonadState<F, S>,
    genK: GenK<F>,
    genS: Gen<S>,
    EQK: EqK<F>,
    eqS: Eq<S>
  ): List<Law> {
    return listOf(
      Law("Monad State Laws: get().followedBy(m) == m") {
        MS.monadStateGetProducesNoSideEffect(genK.genK(Gen.int()), EQK.liftEq(Int.eq()))
      },
      Law("Monad State Laws: set twice eq to set once the last element") {
        MS.monadStateSetTwice(genS, EQK.liftEq(Eq.any()))
      },
      Law("Monad State Laws: set get") { MS.monadStateSetGet(genS, EQK.liftEq(eqS)) },
      Law("Monad State Laws: get set") { MS.monadStateGetSet(EQK.liftEq(Eq.any())) },
      Law("Monad State Laws: get().flatMap { s1 -> get().flatMap { s2 -> f(s1, s2) } } == get().flatMap { s -> f(s, s) }") {
        MS.getProducesTheSameResult(
          genK.genK(Gen.int()).map { { _: S, _: S -> it } },
          EQK.liftEq(Int.eq())
        )
      },
      Law("Monad State Laws: modify derived") {
        MS.modifyDerived(
          Gen.functionAToB(genS), EQK.liftEq(Eq.any())
        )
      },
      Law("Monad State Laws: inspect derived") {
        MS.inspectDerived(
          Gen.functionAToB(Gen.int()), EQK.liftEq(Int.eq())
        )
      },
      Law("Monad State Laws: state derived") {
        MS.stateDerived(
          Gen.functionAToB(Gen.tuple2(genS, Gen.int())), EQK.liftEq(Int.eq())
        )
      }
    )
  }

  fun <F, S> laws(
    MS: MonadState<F, S>,
    GENK: GenK<F>,
    genS: Gen<S>,
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
    genS: Gen<S>,
    EQK: EqK<F>,
    eqS: Eq<S>
  ): List<Law> =
    MonadLaws.laws(MS, FF, AP, SL, GENK, EQK) + monadStateLaws(MS, GENK, genS, EQK, eqS)

  fun <F, S, A> MonadState<F, S>.monadStateGetProducesNoSideEffect(
    genFA: Gen<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFA) { fa ->
      get().followedBy(fa).equalUnderTheLaw(fa, EQ)
    }
  }

  fun <F, S> MonadState<F, S>.monadStateSetTwice(genS: Gen<S>, EQ: Eq<Kind<F, Unit>>) {
    forAll(genS, genS) { s, t ->
      set(s).followedBy(set(t)).equalUnderTheLaw(set(t), EQ)
    }
  }

  fun <F, S> MonadState<F, S>.monadStateSetGet(genS: Gen<S>, EQ: Eq<Kind<F, S>>) {
    forAll(genS) { s ->
      set(s).followedBy(get()).equalUnderTheLaw(set(s).mapConst(s), EQ)
    }
  }

  fun <F, S> MonadState<F, S>.monadStateGetSet(EQ: Eq<Kind<F, Unit>>) {
    get().flatMap { set(it) }.equalUnderTheLaw(just(Unit), EQ)
  }

  fun <F, S, A> MonadState<F, S>.getProducesTheSameResult(
    genFun: Gen<(S, S) -> Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { f ->
      get().flatMap { s1 -> get().flatMap { s2 -> f(s1, s2) } }
        .equalUnderTheLaw(get().flatMap { s -> f(s, s) }, EQ)
    }
  }

  fun <F, S> MonadState<F, S>.modifyDerived(
    genFun: Gen<(S) -> S>,
    EQ: Eq<Kind<F, Unit>>
  ) {
    forAll(genFun) { f ->
      modify(f).equalUnderTheLaw(get().flatMap { set(f(it)) }, EQ)
    }
  }

  fun <F, S, A> MonadState<F, S>.inspectDerived(
    genFun: Gen<(S) -> A>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { f ->
      inspect(f).equalUnderTheLaw(get().map(f), EQ)
    }
  }

  fun <F, S, A> MonadState<F, S>.stateDerived(
    genFun: Gen<(S) -> Tuple2<S, A>>,
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
