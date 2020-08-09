package arrow.android.binding.core

import android.widget.CompoundButton
import arrow.fx.coroutines.CancelToken
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.cancellable

fun <V : CompoundButton> V.checkedStateChanges(): Stream<Boolean> =
  Stream.cancellable {
    setOnCheckedChangeListener { _, isChecked ->
      emit(isChecked)
    }
    CancelToken { setOnCheckedChangeListener(null) }
  }
