package arrow.android.demo.cases.gameoflife

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import arrow.android.binding.core.checkedStateChanges
import arrow.android.binding.core.clicks
import arrow.android.demo.R
import arrow.android.demo.utils.lifecycleScope
import arrow.android.recyclerview.LayoutViewHolder
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.compile
import arrow.fx.coroutines.stream.concurrent.SignallingAtomic
import arrow.fx.coroutines.stream.filterNull
import arrow.fx.coroutines.stream.parJoinUnbounded
import arrow.fx.coroutines.stream.scan
import kotlinx.android.synthetic.main.fragment_demos_list.list
import kotlinx.android.synthetic.main.fragment_game_of_life.*
import kotlinx.android.synthetic.main.item_node.view.*
import kotlinx.coroutines.delay
import kotlin.properties.Delegates

class GameOfLifeFragment : Fragment(R.layout.fragment_game_of_life) {

  private val nodeAdapter = NodeAdapter()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    with(list) {
      layoutManager = GridLayoutManager(requireContext(), columnCount)
      adapter = nodeAdapter
    }

    val nodes: List<List<Node>> =
      (0 until rowCount).map { x ->
        (0 until columnCount).map { y -> Node(coords = Point(x, y)) }
      }

    lifecycleScope.launchWhenCreated {
      val clock = Stream(Event.Step).repeat().effectTap { delay(1_000) }
      val events = Stream(
        nodeAdapter.selection.map { Event.NodeSelected(it) },
        start_stop_button.clicks().map { Event.StartStop },
        clock,
        step_button.clicks().map { Event.ManualStep }
      ).parJoinUnbounded()

      events.scan(State(nodes)) { state, event ->
        when (event) {
          is Event.NodeSelected -> state.copy(nodes = state.nodes.map { col ->
            col.map { node -> node.takeUnless { it.coords == event.node.coords } ?: event.node }
          })
          is Event.StartStop -> state.copy(stopped = !state.stopped)
          is Event.Step -> state.takeIf { it.stopped } ?: state.step()
          is Event.ManualStep -> state.step()
        }
      }.effectTap {
        // TODO(pabs): handle main thread correctly
        requireView().post {
          start_stop_button.text = getString(if (it.stopped) R.string.start else R.string.stop)
        }
        nodeAdapter.submitList(it.nodes.flatten())
      }.compile().drain()
    }
  }

  private fun State.step(): State = copy(nodes = nodes.map { col ->
    col.map { node ->
      val localPopulation = node.neighbors(nodes).count { it.alive }
      when {
        node.alive -> node.copy(alive = localPopulation in 2..3)
        node.dead -> node.copy(alive = localPopulation == 3)
        else -> node
      }
    }
  })

  private fun Node.neighbors(nodes: List<List<Node>>) = coords.let { (x, y) ->
    listOf(
      x - 1 to y - 1, x to y - 1, x + 1 to y - 1,
      x - 1 to y, /* Self */ x + 1 to y,
      x - 1 to y + 1, x to y + 1, x + 1 to y + 1
    ).mapNotNull { it.takeIf { (x, y) -> x in nodes.indices && y in nodes[x].indices } }
      .map { (x, y) -> nodes[x][y] }
  }

}

private sealed class Event {
  data class NodeSelected(val node: Node) : Event()
  object StartStop : Event()
  object Step : Event()
  object ManualStep : Event()
}

private data class State(val nodes: List<List<Node>>, val stopped: Boolean = true)

private object NodeDiff : DiffUtil.ItemCallback<Node>() {
  override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean =
    oldItem.coords == newItem.coords

  override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean =
    oldItem == newItem
}

private class NodeAdapter : ListAdapter<Node, NodeViewHolder>(NodeDiff) {

  private val selectionSignal = SignallingAtomic.unsafe(null as Node?)

  val selection = selectionSignal.discrete().filterNull()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder =
    NodeViewHolder(parent).apply {
      parent.lifecycleScope.launchWhenCreated {
        selection.effectTap { node -> selectionSignal.set(node) }.compile().drain()
      }
    }

  override fun getItemCount(): Int = nodeCount

  override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
    holder.node = getItem(position)
  }

}

class NodeViewHolder(parent: ViewGroup) : LayoutViewHolder(parent, R.layout.item_node) {

  private val nodeView = itemView.node_view

  var node by Delegates.observable<Node?>(null) { _, _, new ->
      nodeView.isChecked = new?.alive == true
  }

  val selection: Stream<Node> = nodeView.checkedStateChanges()
    .mapNotNull { selected -> node?.copy(alive = selected) }
}

data class Node(val alive: Boolean = false, val coords: Point) {
  val dead get() = !alive
}

private const val rowCount = 10
private const val columnCount = 10
private const val nodeCount = rowCount * columnCount
