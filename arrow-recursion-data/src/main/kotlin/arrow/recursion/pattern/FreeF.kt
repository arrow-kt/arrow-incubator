package arrow.recursion.pattern

import arrow.recursion.data.Fix

sealed class FreeF<E, A> {
  data class Pure<E, A>(val e: E) : FreeF<E, A>()
  data class Impure<E, A>(val fa: suspend () -> A) : FreeF<E, A>()

  fun <B> map(f: (A) -> B): FreeF<E, B> = when (this) {
    is Pure -> Pure(e)
    is Impure -> Impure { f(fa()) }
  }

  companion object {
    fun <A> pure(a: A): FreeR<A> =
      Fix { Pure<A, Any?>(a) }
    fun <A> impure(fa: suspend () -> FreeR<A>): FreeR<A> =
      Fix { Impure(fa) }
  }
}

typealias FreeR<A> = Fix<FreeF<A, *>>
