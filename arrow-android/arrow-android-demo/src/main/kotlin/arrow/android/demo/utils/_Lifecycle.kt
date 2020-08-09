package arrow.android.demo.utils

import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

val View.lifecycleScope: LifecycleCoroutineScope
  get() = checkNotNull(context as? LifecycleOwner) { "$context should be a LifecycleOwner" }.lifecycleScope

fun LifecycleOwner.whenCreated(block: suspend CoroutineScope.() -> Unit) {
  lifecycleScope.launchWhenCreated(block = block)
}
