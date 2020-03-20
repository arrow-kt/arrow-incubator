package arrow.mtl.test.eq

import arrow.Kind
import arrow.mtl.Kleisli
import arrow.mtl.KleisliPartialOf
import arrow.mtl.fix
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK

fun <D, F> Kleisli.Companion.eqK(EQKF: EqK<F>, d: D) = object : EqK<KleisliPartialOf<D, F>> {
  override fun <A> Kind<KleisliPartialOf<D, F>, A>.eqK(other: Kind<KleisliPartialOf<D, F>, A>, EQ: Eq<A>): Boolean =
    (this.fix() to other.fix()).let {
      val ls = it.first.run(d)
      val rs = it.second.run(d)

      EQKF.liftEq(EQ).run {
        ls.eqv(rs)
      }
    }
}
