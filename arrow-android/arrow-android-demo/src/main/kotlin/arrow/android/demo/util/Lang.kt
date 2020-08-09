package arrow.android.demo.util

// TODO(pabs): Propose for arrow-syntax
inline fun <reified T> Any?.castAs(): T? = this as? T
