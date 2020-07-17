package arrow.validation.test

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

fun Arb.Companion.lessThan(max: Int): Arb<Int> = Arb.int().filter { it < max }

fun Arb.Companion.lessEqual(max: Int): Arb<Int> = Arb.int().filter { it <= max }

fun Arb.Companion.greaterThan(min: Int): Arb<Int> = Arb.int().filter { it > min }

fun Arb.Companion.greaterEqual(min: Int): Arb<Int> = Arb.int().filter { it >= min }

fun Arb.Companion.greaterOrEqThan(max: Int): Arb<Int> = Arb.int().filter { it >= max }

fun Arb.Companion.nonEmptyString(): Arb<String> = Arb.string().filter { it.isNotEmpty() }
