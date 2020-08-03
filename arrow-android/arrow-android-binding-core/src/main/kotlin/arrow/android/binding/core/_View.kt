package arrow.android.binding.core

import android.annotation.TargetApi
import android.os.Build
import android.view.DragEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import arrow.android.binding.core.LayoutChange.Bounds
import arrow.fx.coroutines.onCancel
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.callback

val <V : View> V.clicks: Stream<Unit>
  get() = Stream.callback {
    setOnClickListener { emit(Unit) }
    onCancel({}, { setOnClickListener(null) })
  }

val <V : View> V.longClicks: PartialStream<Unit, Boolean>
  get() = { applyReturn ->
    Stream.callback {
      setOnLongClickListener {
        emit(Unit)
        applyReturn(Unit)
      }
      onCancel({}, { setOnLongClickListener(null) })
    }
  }

val <V : View> V.keys: PartialStream<OnKey, Boolean>
  get() = { applyReturn ->
    Stream.callback {
      setOnKeyListener { _, keyCode, event ->
        val onKey = OnKey(keyCode, event)
        emit(onKey)
        applyReturn(onKey)
      }
      onCancel({}, { setOnKeyListener(null) })
    }
  }

data class OnKey(val keyCode: Int, val event: KeyEvent)

@get:TargetApi(Build.VERSION_CODES.M)
val <V : View> V.scrollChangeEvents: Stream<ScrollChange>
  get() = Stream.callback {
    setOnScrollChangeListener { _, x, y, oldX, oldY ->
      emit(ScrollChange(new = Scroll(x, y), old = Scroll(oldX, oldY)))
    }
    onCancel({}, { setOnScrollChangeListener(null) })
  }

data class Scroll(val x: Int, val y: Int)
data class ScrollChange(val new: Scroll, val old: Scroll)

val <V : View> V.attachStates: Stream<Boolean>
  get() = Stream.callback {
    val listener = object : View.OnAttachStateChangeListener {
      override fun onViewDetachedFromWindow(v: View): Unit = emit(false)
      override fun onViewAttachedToWindow(v: View): Unit = emit(true)
    }
    addOnAttachStateChangeListener(listener)
    onCancel({ false }, onCancel = { removeOnAttachStateChangeListener(listener) })
  }

val <V : View> V.drags: PartialStream<DragEvent, Boolean>
  get() = { applyReturn ->
    Stream.callback {
      setOnDragListener { _, event ->
        emit(event)
        applyReturn(event)
      }
      onCancel({ }, { setOnDragListener(null) })
    }
  }

val <V : View> V.focusChanges: Stream<Boolean>
  get() = Stream.callback {
    setOnFocusChangeListener { _, hasFocus -> emit(hasFocus) }
    onCancel({}, { onFocusChangeListener = null })
  }

val <V : View> V.hovers: PartialStream<MotionEvent, Boolean>
  get() = { applyReturn ->
    Stream.callback {
      setOnHoverListener { _, event ->
        emit(event)
        applyReturn(event)
      }
      onCancel({}, { setOnHoverListener(null) })
    }
  }


val <V : View> V.layoutChanges: Stream<LayoutChange>
  get() = Stream.callback {
    val callback: (View, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit = { _, l, t, r, b, oL, oT, oR, oB ->
      emit(LayoutChange(old = Bounds(oL, oT, oR, oB), new = Bounds(l, t, r, b)))
    }
    addOnLayoutChangeListener(callback)
    onCancel({ }, { removeOnLayoutChangeListener(callback) })
  }

data class LayoutChange(val old: Bounds, val new: Bounds) {
  data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)
}

val <V : View> V.visibilities: Stream<Visibility>
  get() = Stream.callback {
    val listener: (Int) -> Unit = { emit(Visibility(it)) }
    setOnSystemUiVisibilityChangeListener(listener)
    onCancel({}, { setOnSystemUiVisibilityChangeListener(null) })
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
