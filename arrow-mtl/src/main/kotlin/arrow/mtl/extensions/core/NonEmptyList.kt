package arrow.mtl.extensions.core

import arrow.core.ForNonEmptyList
import arrow.core.NonEmptyList
import arrow.core.extensions.nonemptylist.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun NonEmptyList.Companion.monadBase(): MonadBase<ForNonEmptyList, ForNonEmptyList> = MonadBase.id(NonEmptyList.monad())

fun NonEmptyList.Companion.monadBaseControl(): MonadBaseControl<ForNonEmptyList, ForNonEmptyList> = MonadBaseControl.id(NonEmptyList.monad())
