package arrow.recursion.pattern

data class Cofree<A>(val head: A, val tail: suspend () -> A) {
  fun <B> map(f: (A) -> B): Cofree<B> = Cofree<B>(f(head), { f(tail()) })
}

data class CofreeF<A, B>(val head: A, val tail: suspend () -> B) {
  fun <C> map(f: (B) -> C): CofreeF<A, C> = CofreeF(head, { f(tail()) })
}

