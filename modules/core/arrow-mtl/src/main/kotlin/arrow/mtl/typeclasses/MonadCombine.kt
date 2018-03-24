package arrow.mtl.typeclasses

import arrow.Kind
import arrow.Kind2
import arrow.core.Tuple2
import arrow.typeclasses.Alternative
import arrow.typeclasses.Bifoldable
import arrow.typeclasses.Foldable

/**
 * The combination of a Monad with a MonoidK
 */
interface MonadCombine<F> : MonadFilter<F>, Alternative<F> {

    fun <G, A> Kind<F, Kind<G, A>>.unite(FG: Foldable<G>): Kind<F, A> =
            flatMap(this, { ga -> FG.foldLeft(ga, empty<A>(), { acc, a -> combineK(acc, pure(a)) }) })

    fun <G, A, B> Kind<F, Kind2<G, A, B>>.separate(BFG: Bifoldable<G>): Tuple2<Kind<F, A>, Kind<F, B>> {
        val asep = flatMap(this, { gab -> BFG.run { algebra<A>().bifoldMap(gab, { pure(it) }, { _ -> empty() }) } })
        val bsep = flatMap(this, { gab -> BFG.run { algebra<B>().bifoldMap(gab, { _ -> empty() }, { pure(it) }) } })
        return Tuple2(asep, bsep)
    }
}
