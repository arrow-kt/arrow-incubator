package arrow.mtl.extensions.core

import arrow.core.Eval
import arrow.core.ForEval
import arrow.core.extensions.eval.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun Eval.Companion.monadBase(): MonadBase<ForEval, ForEval> = MonadBase.id(Eval.monad())

fun Eval.Companion.monadBaseControl(): MonadBaseControl<ForEval, ForEval> = MonadBaseControl.id(Eval.monad())
