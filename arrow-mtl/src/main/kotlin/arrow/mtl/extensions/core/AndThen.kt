package arrow.mtl.extensions.core

import arrow.core.AndThen
import arrow.core.AndThenPartialOf
import arrow.core.extensions.andthen.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun <A> AndThen.Companion.monadBase(): MonadBase<AndThenPartialOf<A>, AndThenPartialOf<A>> = MonadBase.id(AndThen.monad())

fun <A> AndThen.Companion.monadBaseControl(): MonadBaseControl<AndThenPartialOf<A>, AndThenPartialOf<A>> =
  MonadBaseControl.id(AndThen.monad())
