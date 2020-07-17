package arrow.recursion

import arrow.core.Eval
import arrow.core.ForOption
import arrow.core.None
import arrow.core.Option
import arrow.core.extensions.eval.monad.monad
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.traverse.traverse
import arrow.core.fix
import arrow.core.none
import arrow.core.some
import arrow.recursion.data.Fix
import arrow.recursion.extensions.fix.birecursive.birecursive
import arrow.core.test.UnitSpec
import arrow.core.value
import arrow.recursion.test.BirecursiveLaws
import arrow.typeclasses.Eq
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map

class FixBirecursive : UnitSpec() {
  init {
    testLaws(
      BirecursiveLaws.laws(
        Option.traverse(),
        Fix.birecursive(Option.functor()),
        Arb.list(Arb.int()).map { it.toFix() },
        Arb.constant((0..5000).toList()).map { it.toFix() },
        Eq.any(),
        {
          it.fix().fold({ 0 }, { it + 1 })
        },
        {
          Eval.now(it.fix().fold({ 0 }, { it + 1 }))
        },
        {
          when (it) {
            0 -> none()
            else -> (it - 1).some()
          }
        },
        {
          Eval.later {
            when (it) {
              0 -> none()
              else -> (it - 1).some()
            }
          }
        }
      )
    )
  }
}

fun <A> List<A>.toFix(): Fix<ForOption> = Fix.birecursive(Option.functor()).run {
  this@toFix.anaM(Option.traverse(), Eval.monad()) {
    Eval.later {
      if (it.isEmpty()) None
      else it.drop(1).some()
    }
  }.value()
}
