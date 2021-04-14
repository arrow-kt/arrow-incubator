package arrow.free

import arrow.Kind
import arrow.core.ForOption
import arrow.core.FunctionK
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.nonemptylist.monad.monad
import arrow.core.extensions.option.foldable.foldable
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.traverse.traverse
import arrow.free.extensions.FreeEq
import arrow.free.extensions.FreeMonad
import arrow.free.extensions.free.applicative.applicative
import arrow.free.extensions.free.eq.eq
import arrow.free.extensions.free.foldable.foldable
import arrow.free.extensions.free.functor.functor
import arrow.free.extensions.free.monad.monad
import arrow.free.extensions.free.traverse.traverse
import arrow.free.extensions.fx
import arrow.higherkind
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.laws.EqLaws
import arrow.core.test.laws.FoldableLaws
import arrow.core.test.laws.MonadLaws
import arrow.core.test.laws.TraverseLaws
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe

@higherkind
sealed class Ops<out A> : OpsOf<A> {

  data class Value(val a: Int) : Ops<Int>()
  data class Add(val a: Int, val y: Int) : Ops<Int>()
  data class Subtract(val a: Int, val y: Int) : Ops<Int>()

  companion object : FreeMonad<ForOps> {
    fun value(n: Int): Free<ForOps, Int> = Free.liftF(Value(n))
    fun add(n: Int, y: Int): Free<ForOps, Int> = Free.liftF(Add(n, y))
    fun subtract(n: Int, y: Int): Free<ForOps, Int> = Free.liftF(Subtract(n, y))
  }
}

class FreeTest : UnitSpec() {

  private val program = Ops.fx.monad {
    val added = !Ops.add(10, 10)
    val subtracted = !Ops.subtract(added, 50)
    subtracted
  }.fix()

  private fun stackSafeTestProgram(n: Int, stopAt: Int): Free<ForOps, Int> = Ops.fx.monad {
    val v = !Ops.add(n, 1)
    val r = !if (v < stopAt) stackSafeTestProgram(v, stopAt) else Free.just(v)
    r
  }.fix()

  init {
    val optionMonad = Option.monad()

    val EQ: FreeEq<ForOps, ForOption, Int> = Free.eq(optionMonad, optionInterpreter)

    fun <S> freeGENK() = object : GenK<FreePartialOf<S>> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<FreePartialOf<S>, A>> =
        gen.map {
          it.free<S, A>()
        }
    }

    val opsEQK = object : EqK<FreePartialOf<ForOps>> {
      override fun <A> Kind<FreePartialOf<ForOps>, A>.eqK(other: Kind<FreePartialOf<ForOps>, A>, EQ: Eq<A>): Boolean =
        (this.fix() to other.fix()).let {
          Free.eq<ForOps, ForOption, A>(optionMonad, optionInterpreter).run {
            it.first.eqv(it.second)
          }
        }
    }

    val optionEq = object : EqK<FreePartialOf<ForOption>> {
      override fun <A> Kind<FreePartialOf<ForOption>, A>.eqK(other: Kind<FreePartialOf<ForOption>, A>, EQ: Eq<A>): Boolean =
        (this.fix() to other.fix()).let {
          Free.eq<ForOption, ForOption, A>(Option.monad(), FunctionK.id()).run {
            it.first.eqv(it.second)
          }
        }
    }

    fun opsGENK() = object : GenK<FreePartialOf<ForOps>> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<FreePartialOf<ForOps>, A>> =
        Gen.ops(Gen.int()) as Gen<Kind<FreePartialOf<ForOps>, A>>
    }

    testLaws(
      EqLaws.laws(EQ, Gen.ops(Gen.int())),
      // TODO
      // MonadLaws.laws(Ops, opsGENK(), opsEQK),
      MonadLaws.laws(Free.monad(), Free.functor(), Free.applicative(), Free.monad(), freeGENK(), optionEq),
      FoldableLaws.laws(Free.foldable(Option.foldable()), freeGENK()),
      TraverseLaws.laws(Free.traverse(Option.traverse()), freeGENK(), optionEq)
    )

    "Can interpret an ADT as Free operations to Option" {
      program.foldMap(optionInterpreter, Option.monad()) shouldBe Some(-30)
    }

    "Can interpret an ADT as Free operations to NonEmptyList" {
      program.foldMap(nonEmptyListInterpreter, NonEmptyList.monad()) shouldBe NonEmptyList.of(-30)
    }

    "foldMap is stack safe" {
      val n = 50000
      val hugeProg = stackSafeTestProgram(0, n)
      hugeProg.foldMap(optionInterpreter, optionMonad) shouldBe Some(n)
    }

    "free should support fx syntax" {
      val n1 = 1
      val n2 = 2
      Free.fx<ForOption, Int> {
        val v1 = Free.just<ForOption, Int>(n1).bind()
        val v2 = Free.just<ForOption, Int>(n2).bind()
        v1 + v2
      }.run(Option.monad()) shouldBe Some(n1 + n2)
    }
  }
}

private fun Gen.Companion.ops(gen: Gen<Int>) =
  Gen.oneOf(
    gen.map { Ops.value(it) },
    Gen.bind(gen, gen) { a, b ->
      Ops.add(a, b)
    },
    Gen.bind(gen, gen) { a, b ->
      Ops.subtract(a, b)
    }
  )
