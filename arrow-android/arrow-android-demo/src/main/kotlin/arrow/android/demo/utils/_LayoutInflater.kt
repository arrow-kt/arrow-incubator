package arrow.android.demo.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflateDetached(@LayoutRes layout: Int): View =
    layoutInflater.inflate(layout, this, false)

private val ViewGroup.layoutInflater
    get() = LayoutInflater.from(context)
