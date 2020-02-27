package arrow.mtl.extensions.core

import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.extensions.either.monad.monad
import arrow.mtl.typeclasses.MonadBase
import arrow.mtl.typeclasses.MonadBaseControl

fun <L> Either.Companion.monadBase(): MonadBase<EitherPartialOf<L>, EitherPartialOf<L>> = MonadBase.id(Either.monad())

fun <L> Either.Companion.monadBaseControl(): MonadBaseControl<EitherPartialOf<L>, EitherPartialOf<L>> = MonadBaseControl.id(Either.monad())
