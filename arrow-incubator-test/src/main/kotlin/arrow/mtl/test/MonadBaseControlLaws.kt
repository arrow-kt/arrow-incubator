package arrow.mtl.test

import arrow.Kind
import arrow.core.extensions.eq
import arrow.core.test.generators.GenK
import arrow.core.test.generators.functionAToB
import arrow.core.test.laws.Law
import arrow.core.test.laws.equalUnderTheLaw
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadBaseControlLaws {
  fun <B, M> laws(
    MBC: MonadBaseControl<B, M>,
    genM: GenK<M>,
    genB: GenK<B>,
    EQM: EqK<M>
  ): List<Law> = MonadBaseLaws.laws(MBC, genB, EQM) + listOf(
    Law("MonadBaseControl: Identity") {
      MBC.identity(Gen.int(), EQM.liftEq(Int.eq()))
    },
    Law("MonadBaseControl: Associativity") {
      MBC.associativity(
        genB.genK(Gen.int()),
        Gen.functionAToB(genB.genK(Gen.int())),
        EQM.liftEq(Int.eq())
      )
    },
    Law("MonadBaseControl: liftBaseDerived") {
      MBC.liftBaseDerived(genB.genK(Gen.int()), EQM.liftEq(Int.eq()))
    },
    Law("MonadBaseControl: liftBaseWith (\r -> r m) >>= restoreM == m") {
      MBC.restoreMAfterLiftBaseWith(genM.genK(Gen.int()), EQM.liftEq(Int.eq()))
    }
  )

  private fun <B, M, A> MonadBaseControl<B, M>.identity(
    genA: Gen<A>,
    eq: Eq<Kind<M, A>>
  ) {
    forAll(genA) { a ->
      liftBaseWith { MB().just(a) }
        .equalUnderTheLaw(MM().just(a), eq)
    }
  }

  private fun <B, M, A, C> MonadBaseControl<B, M>.associativity(
    genBA: Gen<Kind<B, A>>,
    genFun: Gen<(A) -> Kind<B, C>>,
    eqM: Eq<Kind<M, C>>
  ) {
    forAll(genBA, genFun) { ba, fba ->
      MB().run { liftBaseWith { ba.flatMap(fba) } }
        .equalUnderTheLaw(
          MM().run { liftBaseWith { ba }.flatMap { a -> liftBaseWith { fba(a) } } },
          eqM
        )
    }
  }

  private fun <B, M, A> MonadBaseControl<B, M>.liftBaseDerived(
    genBA: Gen<Kind<B, A>>,
    eqM: Eq<Kind<M, A>>
  ) {
    forAll(genBA) { ba ->
      liftBaseWith { ba }.equalUnderTheLaw(ba.liftBase(), eqM)
    }
  }

  private fun <B, M, A> MonadBaseControl<B, M>.restoreMAfterLiftBaseWith(
    genM: Gen<Kind<M, A>>,
    eqM: Eq<Kind<M, A>>
  ) {
    forAll(genM) { ma ->
      MM().run { liftBaseWith { runInBase -> runInBase(ma) }.flatMap { it.restoreM() } }
        .equalUnderTheLaw(ma, eqM)
    }
  }
}
