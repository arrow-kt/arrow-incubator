package arrow.mtl.test.eq

import arrow.Kind
import arrow.core.Tuple2
import arrow.core.extensions.tuple2.eq.eq
import arrow.mtl.StateT
import arrow.mtl.StateTPartialOf
import arrow.mtl.fix
import arrow.mtl.run
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad

fun <S, F> StateT.Companion.eqK(EQKF: EqK<F>, EQS: Eq<S>, M: Monad<F>, s: S) = object : EqK<StateTPartialOf<S, F>> {
  override fun <A> Kind<StateTPartialOf<S, F>, A>.eqK(other: Kind<StateTPartialOf<S, F>, A>, EQ: Eq<A>): Boolean =
    (this.fix() to other.fix()).let {
      val ls = it.first.run(s)
      val rs = it.second.run(s)

      EQKF.liftEq(Tuple2.eq(EQS, EQ)).run {
        ls.eqv(rs)
      }
    }
}
