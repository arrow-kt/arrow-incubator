package arrow.mtl.test

import arrow.Kind
import arrow.core.AndThen
import arrow.core.AndThenPartialOf
import arrow.core.Either
import arrow.core.Eval
import arrow.core.Eval.Now
import arrow.core.ForEval
import arrow.core.ForFunction0
import arrow.core.ForTry
import arrow.core.Function0
import arrow.core.Function1
import arrow.core.Function1PartialOf
import arrow.core.Id
import arrow.core.Ior
import arrow.core.ListK
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.SequenceK
import arrow.core.Try
import arrow.core.extensions.`try`.eq.eq
import arrow.core.extensions.either.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.function0.comonad.extract
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.ior.eqK.eqK
import arrow.core.extensions.listk.eqK.eqK
import arrow.core.extensions.nonemptylist.eqK.eqK
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.semigroup
import arrow.core.extensions.sequencek.eqK.eqK
import arrow.core.fix
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.value
import arrow.mtl.extensions.core.monadBaseControl
import arrow.typeclasses.Eq
import arrow.typeclasses.EqK
import io.kotlintest.properties.Gen

class AndThenTest : UnitSpec() {
  init {
    val genK = object : GenK<AndThenPartialOf<Int>> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<AndThenPartialOf<Int>, A>> =
        gen.map { AndThen { _: Int -> it } }
    }

    val eqK = object : EqK<AndThenPartialOf<Int>> {
      override fun <A> Kind<AndThenPartialOf<Int>, A>.eqK(other: Kind<AndThenPartialOf<Int>, A>, EQ: Eq<A>): Boolean =
        EQ.run { fix()(0).eqv(other.fix()(0)) }
    }

    testLaws(
      MonadBaseControlLaws.laws(
        AndThen.monadBaseControl(),
        genK, genK, eqK
      )
    )
  }
}

class EitherTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        Either.monadBaseControl(),
        Either.genK(Gen.string()),
        Either.genK(Gen.string()),
        Either.eqK(String.eq())
      )
    )
  }
}

class EvalTest : UnitSpec() {
  init {
    val genK = object : GenK<ForEval> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<ForEval, A>> = gen.map(::Now)
    }
    val eqK = object: EqK<ForEval> {
      override fun <A> Kind<ForEval, A>.eqK(other: Kind<ForEval, A>, EQ: Eq<A>): Boolean =
        EQ.run { value().eqv(other.value()) }
    }

    testLaws(
      MonadBaseControlLaws.laws(
        Eval.monadBaseControl(),
        genK, genK, eqK
      )
    )
  }
}

class Function0Test : UnitSpec() {
  init {
    val genK = object : GenK<ForFunction0> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<ForFunction0, A>> =
        gen.map { Function0 { it } }
    }
    val eqK = object : EqK<ForFunction0> {
      override fun <A> Kind<ForFunction0, A>.eqK(other: Kind<ForFunction0, A>, EQ: Eq<A>): Boolean =
        EQ.run { extract().eqv(other.extract()) }
    }

    testLaws(
      MonadBaseControlLaws.laws(
        Function0.monadBaseControl(),
        genK, genK, eqK
      )
    )
  }
}

class Function1Test : UnitSpec() {
  init {
    val genK = object : GenK<Function1PartialOf<Int>> {
      override fun <A> genK(gen: Gen<A>): Gen<Kind<Function1PartialOf<Int>, A>> =
        gen.map { Function1 { _: Int -> it } }
    }
    val eqK = object : EqK<Function1PartialOf<Int>> {
      override fun <A> Kind<Function1PartialOf<Int>, A>.eqK(other: Kind<Function1PartialOf<Int>, A>, EQ: Eq<A>): Boolean =
        EQ.run { fix().f(1).eqv(other.fix().f(1)) }
    }

    testLaws(
      MonadBaseControlLaws.laws(
        Function1.monadBaseControl(),
        genK, genK, eqK
      )
    )
  }
}

class IdTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        Id.monadBaseControl(),
        Id.genK(), Id.genK(), Id.eqK()
      )
    )
  }
}

class IorTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        Ior.monadBaseControl(String.semigroup()),
        Ior.genK(Gen.string()), Ior.genK(Gen.string()),
        Ior.eqK(String.eq())
      )
    )
  }
}

class ListKTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        ListK.monadBaseControl(),
        ListK.genK(), ListK.genK(),
        ListK.eqK()
      )
    )
  }
}

class NonEmptyListTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        NonEmptyList.monadBaseControl(),
        NonEmptyList.genK(), NonEmptyList.genK(),
        NonEmptyList.eqK()
      )
    )
  }
}

class OptionTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        Option.monadBaseControl(),
        Option.genK(), Option.genK(),
        Option.eqK()
      )
    )
  }
}

class SequenceKTest : UnitSpec() {
  init {
    testLaws(
      MonadBaseControlLaws.laws(
        SequenceK.monadBaseControl(),
        SequenceK.genK(), SequenceK.genK(),
        SequenceK.eqK()
      )
    )
  }
}

class TryTest : UnitSpec() {
  init {
    MonadBaseControlLaws.laws(
      Try.monadBaseControl(),
      Try.genK(), Try.genK(),
      object : EqK<ForTry> {
        override fun <A> Kind<ForTry, A>.eqK(other: Kind<ForTry, A>, EQ: Eq<A>): Boolean =
          fix().fold({ t1 ->
            other.fix().fold({ t2 -> t1 == t2 }, { false })
          }, { a1 ->
            other.fix().fold({ false }, { a2 -> EQ.run { a1.eqv(a2) } })
          })
      }
    )
  }
}
