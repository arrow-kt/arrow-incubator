package arrow.mtl.test.eq

import arrow.Kind
import arrow.core.Tuple2
import arrow.core.extensions.tuple2.eq.eq
import arrow.mtl.AccumT
import arrow.mtl.AccumTPartialOf
import arrow.mtl.fix
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK

fun <S, F> AccumT.Companion.eqK(eqkF: EqK<F>, eqS: Eq<S>, s: S) =
  object : EqK<AccumTPartialOf<S, F>> {
    override fun <A> Kind<AccumTPartialOf<S, F>, A>.eqK(other: Kind<AccumTPartialOf<S, F>, A>, EQ: Eq<A>): Boolean =
      (this.fix() to other.fix()).let {
        it.first.runAccumT(s) to it.second.runAccumT(s)
      }.let {
        eqkF.liftEq(Tuple2.eq(eqS, EQ)).run {
          it.first.eqv(it.second)
        }
      }
  }
