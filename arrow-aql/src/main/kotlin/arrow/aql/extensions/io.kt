package arrow.aql.extensions

import arrow.aql.From
import arrow.aql.Select
import arrow.aql.WhereOr
import arrow.extension
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monadError.monadError
import arrow.typeclasses.Applicative
import arrow.typeclasses.Functor
import arrow.typeclasses.MonadError

@extension
interface IOSelect : Select<ForIO> {
  override fun functor(): Functor<ForIO> = IO.functor()
}

@extension
interface IOFrom : From<ForIO> {
  override fun applicative(): Applicative<ForIO> = IO.applicative()
}

@extension
interface IOWhereOr : WhereOr<ForIO, Throwable> {
  override fun monadError(): MonadError<ForIO, Throwable> = IO.monadError()
}
