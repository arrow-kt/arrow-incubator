package arrow.mtl.extensions.core

import arrow.core.Ior
import arrow.core.IorPartialOf
import arrow.core.extensions.ior.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl
import arrow.typeclasses.Semigroup

fun <L> Ior.Companion.monadBase(SL: Semigroup<L>): MonadBase<IorPartialOf<L>, IorPartialOf<L>> = MonadBase.id(Ior.monad(SL))

fun <L> Ior.Companion.monadBaseControl(SL: Semigroup<L>): MonadBaseControl<IorPartialOf<L>, IorPartialOf<L>> = MonadBaseControl.id(Ior.monad(SL))
