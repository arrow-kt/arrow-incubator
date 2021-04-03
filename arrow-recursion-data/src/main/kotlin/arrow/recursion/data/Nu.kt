package arrow.recursion.data

import arrow.recursion.Coalgebra

/**
 * Type level combinator for obtaining the greatest fixed point of a type.
 * This type is the type level encoding of ana.
 */
class Nu<A>(val a: A, val unNu: Coalgebra<A>) {
  companion object {
    operator fun <A> invoke(a: A, unNu: Coalgebra<A>) =
      Nu(a) { unNu(it) }
  }
}
