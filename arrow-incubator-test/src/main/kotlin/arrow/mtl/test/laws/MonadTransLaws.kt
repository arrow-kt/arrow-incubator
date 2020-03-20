package arrow.mtl.test.laws

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
import arrow.mtl.typeclasses.MonadTrans
import arrow.typeclasses.Eq
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadTransLaws {
  fun <T> laws(
    monadTrans: MonadTrans<T>,
    eqT: EqTrans<T>
  ): List<Law> {
    return listOf(
      Law("MonadTrans Laws: Identity") {
        monadTrans.identity(Gen.int(), Id.monad(), eqT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq()))
      },
      Law("MonadTrans Laws: associativity") {
        monadTrans.associativity(
          Gen.functionAToB(Id.genK().genK(Gen.int())),
          Id.genK().genK(Gen.int()),
          Id.monad(),
          eqT.liftEqK(Id.monad(), Id.eqK()).liftEq(Int.eq())
        )
      }
    )
  }

  private fun <T, F, A> MonadTrans<T>.identity(genA: Gen<A>, MF: Monad<F>, eq: Eq<Kind2<T, F, A>>) {
    forAll(genA) { a ->
      liftMonad(MF).just(a).equalUnderTheLaw(MF.just(a).liftT(MF), eq)
    }
  }

  private fun <T, F, A, B> MonadTrans<T>.associativity(
    genFun: Gen<(A) -> Kind<F, B>>,
    genFA: Gen<Kind<F, A>>,
    MF: Monad<F>,
    eq: Eq<Kind2<T, F, B>>
  ) = forAll(genFA, genFun) { fa, ffa ->
    MF.run { fa.flatMap(ffa).liftT(MF) }
      .equalUnderTheLaw(
        liftMonad(MF).run { fa.liftT(MF).flatMap { a -> ffa(a).liftT(MF) } },
        eq
      )
  }
}
