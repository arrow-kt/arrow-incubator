package arrow.mtl.test.laws

import arrow.Kind
import arrow.core.extensions.eq
import arrow.core.identity
import arrow.core.test.generators.GenK
import arrow.core.test.generators.functionAToB
import arrow.core.test.laws.Law
import arrow.core.test.laws.equalUnderTheLaw
import arrow.mtl.typeclasses.MonadReader
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

// Laws mostly from https://mail.haskell.org/pipermail/libraries/2019-October/030038.html
object MonadReaderLaws {
  fun <F, D> laws(
    MR: MonadReader<F, D>,
    genK: GenK<F>,
    genD: Gen<D>,
    eqK: EqK<F>,
    eqD: Eq<D>
  ): List<Law> = listOf(
    Law("MonadReader: ask().followedBy(m) == m") {
      MR.askHasNoSideEffects(genK.genK(Gen.int()), eqK.liftEq(Int.eq()))
    },
    Law("MonadReader: ask().flatMap { s1 -> ask().flatMap { s2 -> k(s1, s2) } } == ask().flatMap { s -> k(s, s) }") {
      MR.askProducesTheSameResult(
        genK.genK(Gen.int()).map { { _: D, _: D -> it } },
        eqK.liftEq(Int.eq())
      )
    },
    Law("MonadReader: ask().local(f) == ask().map(f)") {
      MR.localChangesTheEnvProducedByAsk(Gen.functionAToB(genD), eqK.liftEq(eqD))
    },
    Law("MonadReader: local changes the correct env") {
      MR.localChangesTheCorrectEnv(
        Gen.functionAToB(genD),
        genK.genK(Gen.int()),
        eqK.liftEq(Int.eq())
      )
    },
    Law("MonadReader: local id x == x") {
      MR.localWithIdNoChanges(genK.genK(Gen.int()), eqK.liftEq(Int.eq()))
    },
    Law("MonadReader: x.local(f).local(g) == x.local { g(f(it)) }") {
      MR.localIsAFunctionMorphism(Gen.functionAToB(genD), genK.genK(Gen.int()), eqK.liftEq(Int.eq()))
    },
    Law("MonadReader: just(x).local(f) == just(x)") {
      MR.localPerformsNoSideEffects(Gen.functionAToB(genD), Gen.int(), eqK.liftEq(Int.eq()))
    },
    Law("MonadReader: local f distributes over flatMap") {
      MR.localDistributesOverFlatMap(
        Gen.functionAToB(genD),
        genK.genK(Gen.int()),
        Gen.functionAToB(genK.genK(Gen.int())),
        eqK.liftEq(Int.eq())
      )
    },
    Law("MonadReader: reader derived") {
      MR.readerDerived(Gen.functionAToB(Gen.int()), eqK.liftEq(Int.eq()))
    }
  )

  private fun <F, D, A> MonadReader<F, D>.askHasNoSideEffects(
    genFA: Gen<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFA) { fa ->
      ask().followedBy(fa).equalUnderTheLaw(fa, EQ)
    }
  }

  private fun <F, D, A> MonadReader<F, D>.askProducesTheSameResult(
    genFun: Gen<(D, D) -> Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun) { fa ->
      ask().flatMap { s1 -> ask().flatMap { s2 -> fa(s1, s2) } }.equalUnderTheLaw(ask().flatMap { s -> fa(s, s) }, EQ)
    }
  }

  private fun <F, D> MonadReader<F, D>.localChangesTheEnvProducedByAsk(
    genFun: Gen<(D) -> D>,
    EQ: Eq<Kind<F, D>>
  ) {
    forAll(genFun) { f ->
      ask().local(f).equalUnderTheLaw(ask().map(f), EQ)
    }
  }

 private fun <F, D, A> MonadReader<F, D>.localChangesTheCorrectEnv(
    genFun: Gen<(D) -> D>,
    genFA: Gen<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun, genFA) { f, fa ->
      fa.local(f).equalUnderTheLaw(ask().flatMap { d -> fa.local { _: D -> d } }, EQ)
    }
  }

  private fun <F, D, A> MonadReader<F, D>.localWithIdNoChanges(
    genFA: Gen<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFA) { fa ->
      fa.local(::identity).equalUnderTheLaw(fa, EQ)
    }
  }

  private fun <F, D, A> MonadReader<F, D>.localIsAFunctionMorphism(
    genFun: Gen<(D) -> D>,
    genFA: Gen<Kind<F, A>>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun, genFun, genFA) { f, g, fa ->
      fa.local(f).local(g).equalUnderTheLaw(fa.local { g(f(it)) }, EQ)
    }
  }

  private fun <F, D, A> MonadReader<F, D>.localPerformsNoSideEffects(
    genFun: Gen<(D) -> D>,
    genFA: Gen<A>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genFun, genFA) { f, a ->
      just(a).local(f).equalUnderTheLaw(just(a), EQ)
    }
  }

  private fun <F, D, A, B> MonadReader<F, D>.localDistributesOverFlatMap(
    genFun: Gen<(D) -> D>,
    genFA: Gen<Kind<F, A>>,
    genFunFA: Gen<(A) -> Kind<F, B>>,
    EQ: Eq<Kind<F, B>>
  ) {
    forAll(genFun, genFA, genFunFA) { f, fa, ffa ->
      fa.flatMap(ffa).local(f).equalUnderTheLaw(fa.local(f).flatMap { a -> ffa(a).local(f) }, EQ)
    }
  }

  private fun <F, D, A> MonadReader<F, D>.readerDerived(
    genF: Gen<(D) -> A>,
    EQ: Eq<Kind<F, A>>
  ) {
    forAll(genF) { f ->
      reader(f).equalUnderTheLaw(ask().map(f), EQ)
    }
  }
}
