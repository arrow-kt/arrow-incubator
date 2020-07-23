package arrow.fx.mtl

import arrow.Kind
import arrow.extension
import arrow.fx.IO
import arrow.fx.typeclasses.MonadIO
import arrow.mtl.LogicT
import arrow.mtl.LogicTPartialOf
import arrow.mtl.extensions.LogicTMonad

@extension
interface LogicTMonadIO<M> : MonadIO<LogicTPartialOf<M>>, LogicTMonad<M> {
  fun MIO(): MonadIO<M>

  override fun <A> IO<A>.liftIO(): Kind<LogicTPartialOf<M>, A> =
    LogicT.lift(MIO(), MIO().run { liftIO() })
}
