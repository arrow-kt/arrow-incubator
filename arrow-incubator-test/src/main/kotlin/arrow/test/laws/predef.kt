package arrow.test.laws

import arrow.test.generators.tuple2
import arrow.test.generators.tuple3
import arrow.test.generators.tuple4
import arrow.test.generators.tuple5
import io.kotlintest.properties.Gen

fun <A> forFew(amount: Int, gena: Gen<A>, fn: (a: A) -> Boolean) {
  gena.random().take(amount).toList().map {
    if (!fn(it)) {
      throw AssertionError("Property failed for\n$it)")
    }
  }
}

fun <A, B> forFew(amount: Int, gena: Gen<A>, genb: Gen<B>, fn: (a: A, b: B) -> Boolean) {
  Gen.tuple2(gena, genb).random().take(amount).toList().map {
    if (!fn(it.a, it.b)) {
      throw AssertionError("Property failed for\n${it.a}\n${it.b})")
    }
  }
}

fun <A, B, C> forFew(amount: Int, gena: Gen<A>, genb: Gen<B>, genc: Gen<C>, fn: (a: A, b: B, c: C) -> Boolean) {
  Gen.tuple3(gena, genb, genc).random().take(amount).toList().map {
    if (!fn(it.a, it.b, it.c)) {
      throw AssertionError("Property failed for\n${it.a}\n${it.b}\n${it.c})")
    }
  }
}

fun <A, B, C, D> forFew(amount: Int, gena: Gen<A>, genb: Gen<B>, genc: Gen<C>, gend: Gen<D>, fn: (a: A, b: B, c: C, d: D) -> Boolean) {
  Gen.tuple4(gena, genb, genc, gend).random().take(amount).toList().map {
    if (!fn(it.a, it.b, it.c, it.d)) {
      throw AssertionError("Property failed for\n${it.a}\n${it.b}\n${it.c}\n${it.d})")
    }
  }
}

fun <A, B, C, D, E> forFew(amount: Int, gena: Gen<A>, genb: Gen<B>, genc: Gen<C>, gend: Gen<D>, gene: Gen<E>, fn: (a: A, b: B, c: C, d: D, e: E) -> Boolean) {
  Gen.tuple5(gena, genb, genc, gend, gene).random().take(amount).toList().map {
    if (!fn(it.a, it.b, it.c, it.d, it.e)) {
      throw AssertionError("Property failed for\n${it.a}\n${it.b}\n${it.c}\n${it.d}\n${it.e})")
    }
  }
}
