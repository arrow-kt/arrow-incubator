package arrow.mtl.eq

import arrow.Kind
import arrow.Kind2
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad

interface EqTrans<T> {
  fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<T, F>>
}
