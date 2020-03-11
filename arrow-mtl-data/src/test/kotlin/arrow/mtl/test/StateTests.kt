package arrow.mtl.test

import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.toT
import arrow.core.extensions.id.functor.functor
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.test.UnitSpec
import arrow.mtl.State
import arrow.mtl.StateApi
import arrow.mtl.flatMap
import arrow.mtl.run
import arrow.mtl.runA
import arrow.mtl.runS
import io.kotlintest.shouldBe

class StateTests : UnitSpec() {

  private val addOne = State { n: Int -> n * 2 toT n }

  val add1 = State { n: Int -> n + 1 toT n }

  init {
    "addOne.run(1) should return Pair(2, 1)" {
      addOne.run(1) shouldBe Tuple2(2, 1)
    }

    "addOne.map(n -> n).run(1) should return same Pair(2, 1)" {
      addOne.map(Id.functor()) { n -> n }.run(1) shouldBe Tuple2(2, 1)
    }

    "addOne.map(n -> n.toString).run(1) should return same Pair(2, \"1\")" {
      addOne.map(Id.functor(), Int::toString).run(1) shouldBe Tuple2(2, "1")
    }

    "addOne.runS(1) should return 2" {
      addOne.runS(1) shouldBe 2
    }

    "addOne.runA(1) should return 1" {
      addOne.runA(1) shouldBe 1
    }

    "basic" {
      add1.run(1) shouldBe (2 toT 1)
    }

    "just" {
      val s1 = StateApi.just<String, Int>(1)
      s1.run("foo") shouldBe ("foo" toT 1)
    }

    "get" {
      val s1 = StateApi.get<String>()
      s1.run("foo") shouldBe ("foo" toT "foo")
    }

    "modify" {
      val s1 = StateApi.modify<String> { "bar" }
      val s2 = StateApi.set("bar")
      s1.run("foo") shouldBe s2.run("foo")
    }

    "flatMap is stacksafe" {
      (0..50000).toList().foldLeft(State<Int, Unit> { 0 toT Unit }) { acc, v ->
        acc.flatMap { StateApi.set(v) }
      }.runS(0) shouldBe 50000
    }
  }
}
