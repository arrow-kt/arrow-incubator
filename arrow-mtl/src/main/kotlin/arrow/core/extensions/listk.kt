package arrow.core.extensions

import arrow.Kind
import arrow.core.ForListK
import arrow.core.ListK
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.fix
import arrow.core.k
import arrow.extension
import arrow.typeclasses.MonadLogic
import arrow.typeclasses.MonadPlus

// TODO this is from arrow-core
// should not be merged

@extension
interface ListKMonadPlus : MonadPlus<ForListK>, ListKMonad, ListKAlternative {
  override fun <A, B> Kind<ForListK, A>.ap(ff: Kind<ForListK, (A) -> B>): ListK<B> =
    fix().ap(ff)

  override fun <A, B> Kind<ForListK, A>.map(f: (A) -> B): ListK<B> =
    fix().map(f)

  override fun <A, B, Z> Kind<ForListK, A>.map2(fb: Kind<ForListK, B>, f: (Tuple2<A, B>) -> Z): ListK<Z> =
    fix().map2(fb, f)

  override fun <A> just(a: A): ListK<A> =
    ListK.just(a)
}

@extension
interface ListKMonadLogic : MonadLogic<ForListK>, ListKMonadPlus {

  private fun <E> ListK<E>.tail(): ListK<E> = this.drop(1).k()

  override fun <A> Kind<ForListK, A>.splitM(): Kind<ForListK, Option<Tuple2<Kind<ForListK, A>, A>>> =
    this.fix().let { list ->
      if (list.isEmpty()) {
        just(Option.empty())
      } else {
        just(Option.just(Tuple2(list.tail(), list.first())))
      }
    }
}
