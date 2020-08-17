package arrow.mtl.extensions.core

import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun Id.Companion.monadBase(): MonadBase<ForId, ForId> = MonadBase.id(Id.monad())

fun Id.Companion.monadBaseControl(): MonadBaseControl<ForId, ForId> = MonadBaseControl.id(Id.monad())
