package arrow.mtl.eq

import arrow.Kind
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad

interface EqTrans<T> {
  fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<T, F>>
}
