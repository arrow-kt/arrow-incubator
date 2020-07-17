package arrow.recursion.test

import arrow.core.Eval
import arrow.core.ForEval
import arrow.core.extensions.eval.monad.monad
import arrow.core.value
import arrow.recursion.Algebra
import arrow.recursion.AlgebraM
import arrow.recursion.hylo
import arrow.recursion.typeclasses.Recursive
import arrow.core.test.laws.Law
import arrow.typeclasses.Traverse
import io.kotest.property.Arb
import io.kotest.property.forAll

object RecursiveLaws {

  fun <T, F> laws(
    RR: Recursive<T, F>,
    smallGenT: Arb<T>,
    alg: Algebra<F, Int>
  ): List<Law> = listOf(
    Law("Cata == Hylo + project") { RR.cataEqualsHyloAndProject(smallGenT, alg) },
    Law("Para + algebra instead of r-algebra == cata") { RR.paraEqualsCataWithNormalAlgebra(smallGenT, alg) },
    Law("Histo + algebra instead of cv-algebra == cata") { RR.histoEqualsCataWithNormalAlgebra(smallGenT, alg) }
  )

  fun <T, F> laws(
    TF: Traverse<F>,
    RR: Recursive<T, F>,
    smallGenT: Arb<T>,
    largeGenT: Arb<T>,
    alg: Algebra<F, Int>,
    algM: AlgebraM<F, ForEval, Int>
  ): List<Law> = laws(RR, smallGenT, alg) + listOf(
    Law("cataM with eval is stacksafe") { RR.cataMEvalIsStackSafe(TF, largeGenT, algM) },
    Law("paraM with eval is stacksafe") { RR.paraMEvalIsStackSafe(TF, largeGenT, algM) },
    Law("histoM with eval is stacksafe") { RR.histoMEvalIsStackSafe(TF, largeGenT, algM) }
  )

  private suspend fun <T, F> Recursive<T, F>.cataEqualsHyloAndProject(smallGenT: Arb<T>, alg: Algebra<F, Int>) =
    forAll(5, smallGenT) { t ->
      t.cata(alg) == t.hylo(alg, project(), FF())
    }

  private suspend fun <T, F> Recursive<T, F>.paraEqualsCataWithNormalAlgebra(smallGenT: Arb<T>, alg: Algebra<F, Int>) =
    forAll(5, smallGenT) { t ->
      t.para<Int> {
        alg(FF().run { it.map { it.b } })
      } == t.cata(alg)
    }

  private suspend fun <T, F> Recursive<T, F>.histoEqualsCataWithNormalAlgebra(smallGenT: Arb<T>, alg: Algebra<F, Int>) =
    forAll(5, smallGenT) { t ->
      t.histo<Int> {
        alg(FF().run { it.map { it.head } })
      } == t.cata(alg)
    }

  private suspend fun <T, F> Recursive<T, F>.cataMEvalIsStackSafe(TF: Traverse<F>, largeGenT: Arb<T>, alg: AlgebraM<F, ForEval, Int>) =
    forAll(5, largeGenT) { t ->
      t.cataM(TF, Eval.monad(), alg).value()
      true
    }

  private suspend fun <T, F> Recursive<T, F>.paraMEvalIsStackSafe(TF: Traverse<F>, largeGenT: Arb<T>, alg: AlgebraM<F, ForEval, Int>) =
    forAll(5, largeGenT) { t ->
      t.paraM<ForEval, Int>(TF, Eval.monad()) {
        alg(FF().run { it.map { it.b } })
      }.value()
      true
    }

  private suspend fun <T, F> Recursive<T, F>.histoMEvalIsStackSafe(TF: Traverse<F>, largeGenT: Arb<T>, alg: AlgebraM<F, ForEval, Int>) =
    forAll(5, largeGenT) { t ->
      t.histoM<ForEval, Int>(TF, Eval.monad()) {
        alg(FF().run { it.map { it.head } })
      }.value()
      true
    }
}
