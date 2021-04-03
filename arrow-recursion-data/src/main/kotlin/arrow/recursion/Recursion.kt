package arrow.recursion

import arrow.core.Either
import arrow.core.Eval
import arrow.core.Tuple2
import arrow.core.identity
import arrow.recursion.data.Fix
import arrow.recursion.pattern.Cofree
import arrow.recursion.pattern.FreeF
import arrow.recursion.pattern.FreeR

typealias Algebra<A> = suspend (A) -> A

typealias Coalgebra<A> = suspend (A) -> suspend () -> A

typealias RAlgebra<T, A> = suspend (Tuple2<T, A>) -> A

typealias RCoalgebra<T, A> = (A) -> suspend() ->  Either<T, A>

typealias CVAlgebra<A> = suspend (Cofree<A>) -> A

typealias CVCoalgebra<A> = (A) -> suspend () -> FreeR<A>

/**
 * Combination of cata and ana.
 *
 * An implementation of merge-sort:
 * ```kotlin:ank:playground
 * import arrow.Kind
 * import arrow.recursion.Algebra
 * import arrow.recursion.Coalgebra
 * import arrow.recursion.hylo
 * import arrow.typeclasses.Functor
 *
 * // boilerplate that @higherkind generates
 * class ForTree private constructor()
 * typealias TreeOf<A, B> = Kind<TreePartialOf<A>, B>
 * typealias TreePartialOf<A> = Kind<ForTree, A>
 *
 * // A simple binary tree
 * sealed class Tree<A, B> : TreeOf<A, B> {
 *  class Empty<A, B> : Tree<A, B>()
 *  class Leaf<A, B>(val a: A) : Tree<A, B>()
 *  class Branch<A, B>(val l: B, val r: B) : Tree<A, B>()
 *
 *  companion object {
 *    fun <A> functor(): Functor<TreePartialOf<A>> = object : Functor<TreePartialOf<A>> {
 *      override fun <C, B> Kind<TreePartialOf<A>, C>.map(f: (C) -> B): Kind<TreePartialOf<A>, B> = when (val t = this as Tree<A, C>) {
 *        is Empty -> Empty()
 *        is Leaf -> Leaf(t.a)
 *        is Branch -> Branch(f(t.l), f(t.r))
 *      }
 *    }
 *  }
 * }
 *
 * infix fun List<Int>.merge(other: List<Int>): List<Int> = when {
 *  this.isEmpty() -> other
 *  other.isEmpty() -> this
 *  else ->
 *    if (first() > other.first()) (listOf(other.first()) + (this merge other.drop(1)))
 *    else (listOf(first()) + (this.drop(1) merge other))
 * }
 *
 * fun main() {
 *  val unfold: Coalgebra<TreePartialOf<Int>, List<Int>> = {
 *    when {
 *      it.isEmpty() -> Tree.Empty()
 *      it.size == 1 -> Tree.Leaf(it.first())
 *      else -> (it.size / 2).let { half ->
 *        Tree.Branch<Int, List<Int>>(it.take(half), it.drop(half))
 *      }
 *    }
 *  }
 *  val fold: Algebra<TreePartialOf<Int>, List<Int>> = {
 *    (it as Tree<Int, List<Int>>).let { t ->
 *      when (t) {
 *        is Tree.Empty -> emptyList()
 *        is Tree.Leaf -> listOf(t.a)
 *        is Tree.Branch -> t.l merge t.r
 *      }
 *    }
 *  }
 *
 *  (0..1000).shuffled().also(::println).hylo(fold, unfold, Tree.functor()).also(::println)
 * }
 *
 * ```
 *
 * Note: Not stack-safe. Use [hyloM] with a stack-safe monad, like [Eval]
 */
suspend fun <A, B> A.hylo(
  alg: Algebra<B>,
  coalg: Coalgebra<A>
): B {
  suspend fun h(a: A): B = alg( h(coalg(a)()))
  return h(this)
}


/**
 * Combination of futu and histo
 */
suspend fun <A, B> A.chrono(
  alg: CVAlgebra<B>,
  coalg: CVCoalgebra<A>
): B =
  FreeF.pure(this)
    .hylo<FreeR<A>, Cofree<B>>({
      Cofree(alg(it), it.tail)
    }, {
      when (val fa = it.unfix()) {
        is FreeF.Pure -> coalg(fa.e)
        is FreeF.Impure -> suspend { Fix { fa } }
      }
    }).head

/**
 * Monadic version of chrono
 */

/**
 * Combination of ana + histo
 *
 * Useful to build up a recursive data structure and fold it with the implicit result caching histo provides.
 */
suspend fun <A, B> A.dyna(
  alg: CVAlgebra<B>,
  coalg: Coalgebra<A>
): B =
  hylo<A, Cofree<B>>({
    Cofree(alg(it), it.tail)
  }, coalg).head

/**
 * Refold, but with the ability to short circuit during construction
 */
suspend fun <A, B> B.elgot(alg: Algebra<A>, f: (B) -> Either<A, suspend () -> B>): A {
  suspend fun h(b: B): A = f(b).fold(::identity) { alg(h(it())) }
  return h(this)
}

/**
 * Refold but may short circuit at any time during deconstruction
 */
suspend fun <A, B> A.coelgot(f: (Pair<A, suspend () -> B>) -> B, coalg: Coalgebra<A>): B {
  suspend fun h(a: A): B = f(Pair(a, { h(coalg(a)()) }))
  return h(this)
}
