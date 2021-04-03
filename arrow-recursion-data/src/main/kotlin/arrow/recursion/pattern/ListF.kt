package arrow.recursion.pattern

import arrow.recursion.data.Fix

sealed class ListF<A, B> {
  class NilF<A, B> : ListF<A, B>()
  data class ConsF<A, B>(val a: A, val tail: B) : ListF<A, B>()

  fun <S> map(f: (B) -> S): ListF<A, S> = when (this) {
    is NilF -> NilF()
    is ConsF -> ConsF(a, f(tail))
  }
}

typealias ListR<A> = Fix<ListF<A, *>>
