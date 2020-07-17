package arrow.generic

import arrow.core.None
import arrow.core.Option
import arrow.core.Tuple3
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.monoid.monoid
import arrow.core.some
import arrow.core.toT
import arrow.product
import arrow.core.test.UnitSpec
import arrow.core.test.generators.nonEmptyList
import arrow.core.test.generators.option
import arrow.core.test.generators.tuple3
import arrow.core.test.laws.EqLaws
import arrow.core.test.laws.MonoidLaws
import arrow.typeclasses.Applicative
import io.kotest.property.Arb
import io.kotest.property.forAll
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.bind

@product
data class Person(val name: String, val age: Int, val related: Option<Person>) {
  companion object
}

fun genPerson(): Arb<Person> {
  val genRelated =
    Arb.bind(Arb.string(), Arb.int()) { name: String, age: Int -> Person(name, age, None) }
  return Arb.bind(
    Arb.string(),
    Arb.int(),
    Arb.option(genRelated)
  ) { name: String, age: Int, related: Option<Person> -> Person(name, age, related) }
}

fun tuple3PersonGen(): Arb<Tuple3<String, Int, Option<Person>>> =
  Arb.tuple3(Arb.string(), Arb.int(), Arb.option(genPerson()))

suspend inline fun <reified F> Applicative<F>.testPersonApplicative() {
  forAll(Arb.string(), Arb.int(), genPerson()) { a, b, c ->
    mapToPerson(just(a), just(b), just(c.some())) == just(Person(a, b, c.some()))
  }
}

class ProductTest : UnitSpec() {

  init {

    ".tupled()" {
      forAll(genPerson()) {
        it.tupled() == Tuple3(it.name, it.age, it.related)
      }
    }

    ".toPerson()" {
      forAll(tuple3PersonGen()) {
        it.toPerson() == Person(it.a, it.b, it.c)
      }
    }

    ".tupledLabeled()" {
      forAll(genPerson()) {
        it.tupledLabeled() == Tuple3(
          "name" toT it.name,
          "age" toT it.age,
          "related" toT it.related
        )
      }
    }

    "List<@product>.combineAll()" {
      forAll(Arb.nonEmptyList(genPerson()).map { it.all }) {
        it.combineAll() == it.reduce { a, b -> a + b }
      }
    }

    "Applicative Syntax" {
      Option.applicative().testPersonApplicative()
    }

    "Show instance defaults to .toString()" {
      with(Person.show()) {
        forAll(genPerson()) {
          it.show() == it.toString()
        }
      }
    }

    "Eq instance defaults to .equals()" {
      with(Person.eq()) {
        forAll(genPerson(), genPerson()) { a, b ->
          a.eqv(b) == (a == b)
        }
      }
    }

    "Semigroup combine" {
      forAll(genPerson(), genPerson()) { a, b ->
        with(Person.semigroup()) {
          a.combine(b) == Person(
            a.name + b.name,
            a.age + b.age,
            Option.monoid(this).combineAll(listOf(a.related, b.related))
          )
        }
      }
    }

    "Semigroup + syntax" {
      forAll(genPerson(), genPerson()) { a, b ->
        a + b == Person(
          a.name + b.name,
          a.age + b.age,
          Option.monoid(Person.monoid()).combineAll(listOf(a.related, b.related))
        )
      }
    }

    "Monoid empty" {
      Person.monoid().empty() shouldBe Person("", 0, None)
    }

    "Monoid empty syntax" {
      emptyPerson() shouldBe Person("", 0, None)
    }

    testLaws(
      EqLaws.laws(Person.eq(), genPerson()),
      MonoidLaws.laws(Person.monoid(), genPerson(), Person.eq())
    )
  }
}
