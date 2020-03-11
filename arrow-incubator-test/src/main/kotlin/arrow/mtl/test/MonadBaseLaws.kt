package arrow.mtl.test

import arrow.Kind
import arrow.core.extensions.eq
import arrow.core.test.generators.GenK
import arrow.core.test.generators.functionAToB
import arrow.core.test.laws.Law
import arrow.core.test.laws.equalUnderTheLaw
import arrow.mtl.typeclasses.MonadBase
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadBaseLaws {
  fun <B, M> laws(
    MB: MonadBase<B, M>,
    genB: GenK<B>,
    EQM: EqK<M>
  ): List<Law> = listOf(
    Law("MonadBase: Identity") {
      MB.identity(Gen.int(), EQM.liftEq(Int.eq()))
    },
    Law("MonadBase: Associativity") {
      MB.associativity(
        genB.genK(Gen.int()),
        Gen.functionAToB(genB.genK(Gen.int())),
        EQM.liftEq(Int.eq())
      )
    }
  )

  private fun <B, M, A> MonadBase<B, M>.identity(
    genA: Gen<A>,
    eqM: Eq<Kind<M, A>>
  ) {
    forAll(genA) { a ->
      MB().just(a).liftBase()
        .equalUnderTheLaw(
          MM().just(a),
          eqM
        )
    }
  }

  private fun <B, M, A, C> MonadBase<B, M>.associativity(
    genBA: Gen<Kind<B, A>>,
    genFun: Gen<(A) -> Kind<B, C>>,
    eqM: Eq<Kind<M, C>>
  ) {
    forAll(genBA, genFun) { ba, fba ->
      MB().run { ba.flatMap(fba) }.liftBase()
        .equalUnderTheLaw(
          MM().run { ba.liftBase().flatMap { a -> fba(a).liftBase() } },
          eqM
        )
    }
  }
}
