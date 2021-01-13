package arrow.aql

import arrow.core.Eval
import arrow.core.ForEval
import arrow.core.identity
import arrow.core.toT
import arrow.core.ForListK
import arrow.core.fix
import arrow.core.mapOf
import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.typeclasses.Foldable

interface GroupBy<F> {

  fun foldable(): Foldable<F>

  infix fun <A, Z, X> Query<F, A, Z>.groupBy(group: Z.() -> X): Query<ForEval, Map<X, List<Z>>, Map<X, List<Z>>> =
    foldable().run {
      Query(select = ::identity, from =
      Eval.just(from.foldLeft(emptyMap()) { map, a ->
        val z: Z = select.invoke(a)
        val key: X = group(z)
        val grouped: List<Z> = map[key]?.let { it + z } ?: listOf(z)
        map + mapOf(key toT grouped)
      }))
    }

  fun <Z, X> Query<ForListK, Map<X, List<Z>>, Map<X, List<Z>>>.value(): Map<X, List<Z>> =
    foldable().run {
      this@value.from.fix().firstOrNone().getOrElse { emptyMap() }
    }
}
