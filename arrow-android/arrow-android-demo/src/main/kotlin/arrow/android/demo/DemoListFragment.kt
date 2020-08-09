@file:Suppress("EXPERIMENTAL_API_USAGE")

package arrow.android.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arrow.android.demo.DemoListFragmentDirections.Companion.selectClicksDemo
import arrow.android.demo.DemoListFragmentDirections.Companion.selectGameOfSelectionDemo
import arrow.android.demo.DemoListFragmentDirections.Companion.selectPasswordDemo
import arrow.android.demo.DemoListFragmentDirections.Companion.selectTicTacToeDemo
import arrow.fx.coroutines.Duration
import arrow.fx.coroutines.stream.compile
import kotlinx.android.synthetic.main.fragment_demos_list.*
import java.util.concurrent.TimeUnit

class DemoListFragment : Fragment(R.layout.fragment_demos_list) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleScope.launchWhenCreated {
      val navigation = findNavController()
      NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navigation)
      list.setupAdapter().selectedCase
        .map { demo -> demo.toDirections() }
        .map { directions -> navigation.navigate(directions) }
        .delayBy(Duration(50, TimeUnit.MILLISECONDS))
        .compile()
        .drain()
    }
  }

}

private fun RecyclerView.setupAdapter(): DemosAdapter =
  DemosAdapter().also { demoAdapter ->
    layoutManager = LinearLayoutManager(context)
    adapter = demoAdapter
  }

private fun DemoCase.toDirections(): NavDirections = when (this) {
  DemoCase.Clicks -> selectClicksDemo()
  DemoCase.Login -> selectPasswordDemo()
  DemoCase.GameOfSelection -> selectGameOfSelectionDemo()
  DemoCase.TicTacToe -> selectTicTacToeDemo()
}
