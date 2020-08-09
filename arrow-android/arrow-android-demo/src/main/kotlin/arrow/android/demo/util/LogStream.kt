package arrow.android.demo.util

import android.util.Log
import arrow.fx.coroutines.stream.Stream

fun <O> Stream<O>.effectLog(
  tag: String = "Arrow Android Demo",
  f: (O) -> String = { "$it" }
): Stream<O> = effectTap { Log.i(tag, f(it)) }
