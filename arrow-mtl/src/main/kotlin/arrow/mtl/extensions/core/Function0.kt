package arrow.mtl.extensions.core

import arrow.core.ForFunction0
import arrow.core.Function0
import arrow.core.extensions.function0.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun Function0.Companion.monadBase(): MonadBase<ForFunction0, ForFunction0> = MonadBase.id(Function0.monad())

fun Function0.Companion.monadBaseControl(): MonadBaseControl<ForFunction0, ForFunction0> = MonadBaseControl.id(Function0.monad())
