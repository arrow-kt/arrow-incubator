package arrow.recursion.pattern

import arrow.recursion.data.Fix

data class NonEmptyListF<A, B>(val head: A, val tail: B?) {
  fun <C> map(f: (B) -> C): NonEmptyListF<A, C> = NonEmptyListF(head, tail?.let(f))
}

typealias NonEmptyListR<A> = Fix<NonEmptyListF<A, *>>
