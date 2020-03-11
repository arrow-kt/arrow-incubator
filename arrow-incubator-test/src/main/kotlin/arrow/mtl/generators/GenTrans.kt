package arrow.mtl.generators

import arrow.Kind
import arrow.Kind2
import arrow.core.test.generators.GenK
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen

interface GenTrans<T> {
  fun <F> liftGenK(MF: Monad<F>, genK: GenK<F>): GenK<Kind<T, F>>
}
