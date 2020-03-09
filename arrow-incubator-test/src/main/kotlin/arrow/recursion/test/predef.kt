package arrow.recursion.test

import io.kotlintest.properties.Gen

fun <A> forFew(amount: Int, gena: Gen<A>, fn: (a: A) -> Boolean) {
  gena.random().take(amount).toList().map {
    if (!fn(it)) {
      throw AssertionError("Property failed for\n$it)")
    }
  }
}
