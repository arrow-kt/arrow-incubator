package arrow.mtl.extensions.core

import arrow.core.ForSequenceK
import arrow.core.SequenceK
import arrow.core.extensions.sequencek.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun SequenceK.Companion.monadBase(): MonadBase<ForSequenceK, ForSequenceK> = MonadBase.id(SequenceK.monad())

fun SequenceK.Companion.monadBaseControl(): MonadBaseControl<ForSequenceK, ForSequenceK> = MonadBaseControl.id(SequenceK.monad())
