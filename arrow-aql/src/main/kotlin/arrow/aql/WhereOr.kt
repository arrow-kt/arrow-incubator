package arrow.aql

import arrow.typeclasses.MonadError

interface WhereOr<F, E> {

  fun monadError(): MonadError<F, E>

  infix fun <A, Z> Query<F, A, Z>.where(predicate: A.() -> Boolean): RaisableQuery<F, A, Z> =
    RaisableQuery(select, from, predicate)

  infix fun <A, Z> RaisableQuery<F, A, Z>.orRaise(error: () -> E): Query<F, A, Z> =
    monadError().run {
      Query(
        select,
        from.ensure(error, predicate)
      )
    }
}
