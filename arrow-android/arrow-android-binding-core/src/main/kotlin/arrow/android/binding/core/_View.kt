package arrow.android.binding.core

import android.annotation.TargetApi
import android.os.Build
import android.view.DragEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import arrow.android.binding.core.LayoutChange.Bounds
import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable

val <V : View> V.clicks: Stream<Unit>
  get() = Stream.cancellable {
    setOnClickListener { emit(Unit) }
    CancelToken { setOnClickListener(null) }
  }

val <V : View> V.longClicks: PartialStream<Unit, Boolean>
  get() = { applyReturn ->
    Stream.cancellable {
      setOnLongClickListener {
        emit(Unit)
        applyReturn(Unit)
      }
      CancelToken { setOnLongClickListener(null) }
    }
  }

val <V : View> V.keys: PartialStream<OnKey, Boolean>
  get() = { applyReturn ->
    Stream.cancellable {
      setOnKeyListener { _, keyCode, event ->
        val onKey = OnKey(keyCode, event)
        emit(onKey)
        applyReturn(onKey)
      }
      CancelToken { setOnKeyListener(null) }
    }
  }

data class OnKey(val keyCode: Int, val event: KeyEvent)

@get:TargetApi(Build.VERSION_CODES.M)
val <V : View> V.scrollChangeEvents: Stream<ScrollChange>
  get() = Stream.cancellable {
    setOnScrollChangeListener { _, x, y, oldX, oldY ->
      emit(ScrollChange(new = Scroll(x, y), old = Scroll(oldX, oldY)))
    }
    CancelToken { setOnScrollChangeListener(null) }
  }

data class Scroll(val x: Int, val y: Int)
data class ScrollChange(val new: Scroll, val old: Scroll)

val <V : View> V.attachStates: Stream<Boolean>
  get() = Stream.cancellable {
    val listener = object : View.OnAttachStateChangeListener {
      override fun onViewDetachedFromWindow(v: View): Unit = emit(false)
      override fun onViewAttachedToWindow(v: View): Unit = emit(true)
    }
    addOnAttachStateChangeListener(listener)
    CancelToken { removeOnAttachStateChangeListener(listener) }
  }

val <V : View> V.drags: PartialStream<DragEvent, Boolean>
  get() = { applyReturn ->
    Stream.cancellable {
      setOnDragListener { _, event ->
        emit(event)
        applyReturn(event)
      }
      CancelToken { setOnDragListener(null) }
    }
  }

val <V : View> V.focusChanges: Stream<Boolean>
  get() = Stream.cancellable {
    setOnFocusChangeListener { _, hasFocus -> emit(hasFocus) }
    CancelToken { onFocusChangeListener = null }
  }

val <V : View> V.hovers: PartialStream<MotionEvent, Boolean>
  get() = { applyReturn ->
    Stream.cancellable {
      setOnHoverListener { _, event ->
        emit(event)
        applyReturn(event)
      }
      CancelToken { setOnHoverListener(null) }
    }
  }


val <V : View> V.layoutChanges: Stream<LayoutChange>
  get() = Stream.cancellable {
    val listener: (View, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit = { _, l, t, r, b, oL, oT, oR, oB ->
      emit(LayoutChange(old = Bounds(oL, oT, oR, oB), new = Bounds(l, t, r, b)))
    }
    addOnLayoutChangeListener(listener)
    CancelToken { removeOnLayoutChangeListener(listener) }
  }

data class LayoutChange(val old: Bounds, val new: Bounds) {
  data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)
}

val <V : View> V.visibilities: Stream<Visibility>
  get() = Stream.cancellable {
    val listener: (Int) -> Unit = { emit(Visibility(it)) }
    setOnSystemUiVisibilityChangeListener(listener)
    CancelToken { setOnSystemUiVisibilityChangeListener(null) }
  }

sealed class Visibility {

  companion object {
    operator fun invoke(visibility: Int): Visibility =
      when (visibility) {
        View.VISIBLE -> Visible
        View.INVISIBLE -> Invisible
        View.GONE -> Gone
        else -> error("Visibility of $visibility is not VISIBLE, INVISIBLE or GONE")
      }
  }

  object Visible : Visibility()
  object Invisible : Visibility()
  object Gone : Visibility()
}

typealias PartialStream<A, R> = ((A) -> R) -> Stream<A>
