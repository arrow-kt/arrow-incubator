package arrow.mtl.test

import arrow.Kind
import arrow.Kind2
import arrow.core.Id
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.id.monad.monad
import arrow.core.test.generators.functionAToB
import arrow.core.test.generators.genK
import arrow.core.test.laws.Law
import arrow.core.test.laws.equalUnderTheLaw
import arrow.mtl.eq.EqTrans
import arrow.mtl.generators.GenTrans
import arrow.mtl.typeclasses.MonadTransControl
import arrow.typeclasses.Eq
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadTransControlLaws {
  fun <T> laws(
    MTC: MonadTransControl<T>,
    genTrans: GenTrans<T>,
    EQT: EqTrans<T>
  ): List<Law> =
    MonadTransLaws.laws(MTC, EQT) + listOf(
      Law("MonadTransControl: Identity") {
        MTC.identity(Gen.int(), Id.monad(), EQT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq()))
      },
      Law("MonadTransControl: Associativity") {
        MTC.associativity(
          Gen.functionAToB(Id.genK().genK(Gen.int())),
          Id.genK().genK(Gen.int()),
          Id.monad(),
          EQT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq())
        )
      },
      Law("MonadTransControl: liftT derived") {
        MTC.liftDerived(
          Id.genK().genK(Gen.int()),
          Id.monad(),
          EQT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq())
        )
      },
      Law("MonadTransControl: restoreT restores context") {
        MTC.restoreTAfterLiftWith(
          genTrans.liftGenK(Id.monad(), Id.genK()).genK(Gen.int()),
          Id.monad(),
          EQT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq())
        )
      }
    )

  private fun <T, F, A> MonadTransControl<T>.identity(
    genA: Gen<A>,
    MF: Monad<F>,
    eq: Eq<Kind2<T, F, A>>
  ) {
    forAll(genA) { a ->
      liftWith(MF) { MF.just(a) }
        .equalUnderTheLaw(
          liftMonad(MF).just(a),
          eq
        )
    }
  }

  private fun <T, F, A, B> MonadTransControl<T>.associativity(
    genFun: Gen<(A) -> Kind<F, B>>,
    genFA: Gen<Kind<F, A>>,
    MF: Monad<F>,
    eq: Eq<Kind2<T, F, B>>
  ) {
    forAll(genFA, genFun) { fa, ffa ->
      liftWith(MF) { MF.run { fa.flatMap(ffa) } }
        .equalUnderTheLaw(
          liftMonad(MF).run {
            liftWith(MF) { fa }.flatMap { a ->
              liftWith(MF) { ffa(a) }
            }
          },
          eq
        )
    }
  }

  private fun <T, F, A> MonadTransControl<T>.liftDerived(
    genFA: Gen<Kind<F, A>>,
    MF: Monad<F>,
    eq: Eq<Kind2<T, F, A>>
  ) {
    forAll(genFA) { fa ->
      liftWith(MF) { fa }
        .equalUnderTheLaw(
          fa.liftT(MF),
          eq
        )
    }
  }

  private fun <T, F, A> MonadTransControl<T>.restoreTAfterLiftWith(
    genFT: Gen<Kind2<T, F, A>>,
    MF: Monad<F>,
    eq: Eq<Kind2<T, F, A>>
  ) {
    forAll(genFT) { fta ->
      liftMonad(MF).run {
        liftWith(MF) { runT -> runT(MF, fta) }.flatMap { st ->
          MF.just(st).restoreT(MF)
        }.equalUnderTheLaw(fta, eq)
      }
    }
  }
}
