package arrow.recursion.data

import arrow.recursion.Algebra

/**
 * Type level combinator for obtaining the least fixed point of a type.
 * This type is the type level encoding of cata.
 */
fun interface Mu<A> {
  fun unMu(alg: Algebra<A>): A
}
