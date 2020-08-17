package arrow.fx.mtl

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun IO.Companion.monadBase(): MonadBase<ForIO, ForIO> = MonadBase.id(IO.monad())

fun IO.Companion.monadBaseControl(): MonadBaseControl<ForIO, ForIO> = MonadBaseControl.id(IO.monad())
