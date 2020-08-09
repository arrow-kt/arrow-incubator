package arrow.android.binding.material

import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable
import com.google.android.material.textfield.TextInputLayout

// TODO(pabs): move to arrow-android-binding-material lib
fun <V : TextInputLayout> V.endIconClicks(): Stream<V> = Stream.cancellable {
  setEndIconOnClickListener { emit(this@endIconClicks) }
  CancelToken { setEndIconOnClickListener(null) }
}
