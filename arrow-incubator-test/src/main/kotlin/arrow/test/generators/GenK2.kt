package arrow.test.generators

import arrow.Kind2
import io.kotlintest.properties.Gen

interface GenK2<F> {
  /**
   * lifts Gen<A> and Gen<B> to the context F. the resulting Gen can be used to create types Kind2<F, A, B>
   */
  fun <A, B> genK(genA: Gen<A>, genB: Gen<B>): Gen<Kind2<F, A, B>>
}
