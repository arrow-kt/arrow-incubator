package arrow.mtl.extensions.core

import arrow.core.ForListK
import arrow.core.ListK
import arrow.core.extensions.listk.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun ListK.Companion.monadBase(): MonadBase<ForListK, ForListK> = MonadBase.id(ListK.monad())

fun ListK.Companion.monadBaseControl(): MonadBaseControl<ForListK, ForListK> = MonadBaseControl.id(ListK.monad())
