package arrow.aql

import arrow.core.Eval
import arrow.core.ForEval
import arrow.core.identity
import arrow.core.value
import arrow.typeclasses.Foldable

interface Sum<F> {

  fun foldable(): Foldable<F>

  infix fun <A, Z> Query<F, A, Z>.sum(f: A.() -> Long): Query<ForEval, Long, Long> =
    foldable().run {
      Query(
        select = ::identity,
        from = Eval.now(from.foldLeft(0L) { acc, a ->
          acc + f(a)
        })
      )
    }

  fun Query<ForEval, Long, Long>.value(): Long =
    foldable().run {
      this@value.from.value()
    }
}
