package arrow.android.binding.core

import android.annotation.TargetApi
import android.graphics.Rect
import android.os.Build
import android.view.DragEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import arrow.core.Tuple2
import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable

fun <V : View> V.clicks(): Stream<Unit> = Stream.cancellable {
  // TODO(pabs): Create View/Fragment/Activity dispatcher
  post { setOnClickListener { emit(Unit) } }
  CancelToken { post { setOnClickListener(null) } }
}

fun <V : View> V.longClicks(handle: (V) -> Boolean = AlwaysHandle): Stream<Unit> = Stream.cancellable {
  setOnLongClickListener {
    emit(Unit)
    handle(this@longClicks)
  }
  CancelToken { setOnLongClickListener(null) }
}

fun <V : View, T> V.keys(handle: (V, Int, KeyEvent) -> Boolean = AlwaysHandle): Stream<OnKey> = Stream.cancellable {
  setOnKeyListener { _, keyCode, event ->
    emit(OnKey(keyCode, event))
    handle(this@keys, keyCode, event)
  }
  CancelToken { setOnKeyListener(null) }
}

typealias OnKey = Tuple2<Int, KeyEvent>

@TargetApi(Build.VERSION_CODES.M)
fun <V : View> V.scrollChangeEvents(): Stream<ScrollChange> = Stream.cancellable {
  setOnScrollChangeListener { _, x, y, oldX, oldY ->
    emit(ScrollChange(OldScroll(oldX, oldY), NewScroll(x, y)))
  }
  CancelToken { setOnScrollChangeListener(null) }
}

typealias OldScroll = Tuple2<X, Y>
typealias NewScroll = Tuple2<X, Y>
typealias X = Int
typealias Y = Int
typealias ScrollChange = Tuple2<OldScroll, NewScroll>

fun <V : View> V.attachStates(): Stream<Boolean> = Stream.cancellable {
  val listener = object : View.OnAttachStateChangeListener {
    override fun onViewDetachedFromWindow(v: View): Unit = emit(false)
    override fun onViewAttachedToWindow(v: View): Unit = emit(true)
  }
  addOnAttachStateChangeListener(listener)
  CancelToken { removeOnAttachStateChangeListener(listener) }
}

fun <V : View> V.drags(handle: (DragEvent) -> Boolean = AlwaysHandle): Stream<DragEvent> = Stream.cancellable {
  setOnDragListener { _, event ->
    emit(event)
    handle(event)
  }
  CancelToken { setOnDragListener(null) }
}

fun <V : View> V.focusChanges(): Stream<Boolean> = Stream.cancellable {
  setOnFocusChangeListener { _, hasFocus -> emit(hasFocus) }
  CancelToken { onFocusChangeListener = null }
}

fun <V : View> V.hovers(handle: (MotionEvent) -> Boolean): Stream<MotionEvent> = Stream.cancellable {
  setOnHoverListener { _, event ->
    emit(event)
    handle(event)
  }
  CancelToken { setOnHoverListener(null) }
}

fun <V : View> V.layoutChanges(): Stream<LayoutChange> = Stream.cancellable {
  val listener: (View, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit = { _, l, t, r, b, oL, oT, oR, oB ->
    emit(LayoutChange(OldBounds(oL, oT, oR, oB), NewBounds(l, t, r, b)))
  }
  addOnLayoutChangeListener(listener)
  CancelToken { removeOnLayoutChangeListener(listener) }
}

typealias LayoutChange = Tuple2<OldBounds, NewBounds>
typealias OldBounds = Rect
typealias NewBounds = Rect

fun <V : View> V.visibilities(): Stream<Visibility> = Stream.cancellable {
  val listener: (Int) -> Unit = { emit(Visibility(it)) }
  setOnSystemUiVisibilityChangeListener(listener)
  CancelToken { setOnSystemUiVisibilityChangeListener(null) }
}

enum class Visibility {

  Visible, Invisible, Gone;

  companion object {
    operator fun invoke(visibility: Int): Visibility =
      when (visibility) {
        View.VISIBLE -> Visible
        View.INVISIBLE -> Invisible
        View.GONE -> Gone
        else -> error("Visibility of $visibility is not VISIBLE, INVISIBLE or GONE")
      }
  }

}

private object AlwaysHandle : (Any) -> Boolean, (Any, Any, Any) -> Boolean {
  override fun invoke(p1: Any): Boolean = true
  override fun invoke(p1: Any, p2: Any, p3: Any): Boolean = true
}
