package arrow.mtl.extensions.core

import arrow.core.Function1
import arrow.core.Function1PartialOf
import arrow.core.extensions.function1.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun <I> Function1.Companion.monadBase(): MonadBase<Function1PartialOf<I>, Function1PartialOf<I>> = MonadBase.id(Function1.monad())

fun <I> Function1.Companion.monadBaseControl(): MonadBaseControl<Function1PartialOf<I>, Function1PartialOf<I>> =
  MonadBaseControl.id(Function1.monad())
