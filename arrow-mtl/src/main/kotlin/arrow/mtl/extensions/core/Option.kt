package arrow.mtl.extensions.core

import arrow.core.ForOption
import arrow.core.Option
import arrow.core.extensions.option.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun Option.Companion.monadBase(): MonadBase<ForOption, ForOption> = MonadBase.id(Option.monad())

fun Option.Companion.monadBaseControl(): MonadBaseControl<ForOption, ForOption> = MonadBaseControl.id(Option.monad())
