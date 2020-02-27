package arrow.mtl.extensions.core

import arrow.core.ForTry
import arrow.core.Try
import arrow.core.extensions.`try`.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun Try.Companion.monadBase(): MonadBase<ForTry, ForTry> = MonadBase.id(Try.monad())

fun Try.Companion.monadBaseControl(): MonadBaseControl<ForTry, ForTry> = MonadBaseControl.id(Try.monad())
