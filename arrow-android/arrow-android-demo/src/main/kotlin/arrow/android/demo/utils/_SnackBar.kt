package arrow.android.demo.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.shortSnackbar(text: String) =
  Snackbar.make(this, text, Snackbar.LENGTH_SHORT).show()
