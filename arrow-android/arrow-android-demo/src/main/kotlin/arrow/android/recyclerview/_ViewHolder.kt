package arrow.android.recyclerview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import arrow.android.demo.utils.inflateDetached

open class LayoutViewHolder(parent: ViewGroup, @LayoutRes layoutRes: Int) :
  RecyclerView.ViewHolder(parent.inflateDetached(layoutRes))
