package arrow.android.demo.cases.clicks

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import arrow.android.binding.core.clicks
import arrow.android.binding.core.longClicks
import arrow.android.demo.R
import arrow.android.demo.utils.shortSnackbar
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.compile
import arrow.fx.coroutines.stream.parJoinUnbounded
import kotlinx.android.synthetic.main.fragment_clicks.*

class ClicksFragment : Fragment(R.layout.fragment_clicks) {

  init {
    lifecycleScope.launchWhenStarted {
      events.effectTap { event -> render(event) }.compile().drain()
    }
  }

  private val events = Stream.defer {
    Stream(
      button_a.clicks().map { Event.Click("A") },
      button_a.longClicks().map { Event.LongClick("A") },
      button_b.clicks().map { Event.Click("B") },
      button_b.longClicks().map { Event.LongClick("B") }
    ).effectTap { }
  }.parJoinUnbounded()

  private fun render(state: Event) {
    requireView().shortSnackbar("tapped: $state")
  }
}

private sealed class Event {
  data class Click(val label: String) : Event()
  data class LongClick(val label: String) : Event()
}
