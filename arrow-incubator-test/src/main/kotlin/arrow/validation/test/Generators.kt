package arrow.validation.test

import io.kotlintest.properties.Gen

fun Gen.Companion.lessThan(max: Int): Gen<Int> = Gen.int().filter { it < max }

fun Gen.Companion.lessEqual(max: Int): Gen<Int> = Gen.int().filter { it <= max }

fun Gen.Companion.greaterThan(min: Int): Gen<Int> = Gen.int().filter { it > min }

fun Gen.Companion.greaterEqual(min: Int): Gen<Int> = Gen.int().filter { it >= min }

fun Gen.Companion.greaterOrEqThan(max: Int): Gen<Int> = Gen.int().filter { it >= max }
