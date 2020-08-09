package arrow.android.demo

import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import arrow.android.binding.core.clicks
import arrow.android.demo.utils.inflateDetached
import arrow.android.demo.utils.lifecycleScope
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.compile
import arrow.fx.coroutines.stream.concurrent.SignallingAtomic
import arrow.fx.coroutines.stream.filterNull
import kotlinx.android.synthetic.main.fragment_demos.view.*

class DemosAdapter : RecyclerView.Adapter<DemosAdapter.ViewHolder>() {

  private val _selectedCase: SignallingAtomic<DemoCase?> = SignallingAtomic.unsafe(null)

  val selectedCase: Stream<DemoCase> get() = _selectedCase.discrete().filterNull()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(parent).also {
      parent.lifecycleScope.launchWhenCreated {
        it.selection.effectTap { case -> _selectedCase.set(case) }.compile().drain()
      }
    }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) =
    holder.bind(DemoCase[position])

  override fun getItemCount(): Int = DemoCase.size

  class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    parent.inflateDetached(R.layout.fragment_demos)
  ) {

    private val titleView: TextView = itemView.content
    private var demoCase: DemoCase? = null

    internal fun bind(demoCase: DemoCase) {
      this.demoCase = demoCase
      titleView.text = demoCase.title
    }

    val selection: Stream<DemoCase> = itemView.clicks().mapNotNull { demoCase }
  }

}


