package arrow.android.demo.cases.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import arrow.android.binding.core.clicks
import arrow.android.demo.R
import arrow.android.demo.cases.tictactoe.Event.ResetGame
import arrow.android.demo.cases.tictactoe.Event.TileSelected
import arrow.android.demo.cases.tictactoe.Player.Circle
import arrow.android.demo.cases.tictactoe.Player.Cross
import arrow.android.demo.cases.tictactoe.State.Playing
import arrow.android.demo.cases.tictactoe.State.Tie
import arrow.android.demo.cases.tictactoe.State.Win
import arrow.android.demo.util.effectLog
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.compile
import arrow.fx.coroutines.stream.parJoinUnbounded
import kotlinx.android.synthetic.main.fragment_tic_tac_toe.*

class TicTacToeFragment : Fragment(R.layout.fragment_tic_tac_toe) {

  private val viewModel by viewModels<TicTacToeViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val events = Stream(tileSelection, gameResets).parJoinUnbounded()
    lifecycleScope.launchWhenCreated {
      viewModel.compute(events)
        .effectLog { "State: $it" }
        .effectTap {
          // TODO(pabs): Use correct Scheduler
          requireView().post { render(it) }
        }
        .compile()
        .drain()
    }
  }

  private val tileSelection: Stream<TileSelected>
    // TODO(pabs): Create Stream.array() or constructor overload
    get() = Stream.iterable(tiles.toList())
      .map { tile -> this[tile].clicks().map { tile } }
      .parJoinUnbounded()
      .map { TileSelected(it) }

  private val gameResets: Stream<ResetGame>
    get() = reset_button.clicks().map { ResetGame }

}

private fun Fragment.render(state: State): Unit = when (state) {
  is Playing -> {
    message_view.text = resources.getString(R.string.playing_message, state.nextTurnPlayer.printName)
    render(state.turns)
  }
  is Win -> {
    message_view.text = resources.getString(R.string.win_message, state.player.printName)
    render(state.turns)
  }
  is Tie -> {
    message_view.text = resources.getString(R.string.tie_message)
    render(state.turns)
  }
}

private fun Fragment.render(turns: List<Turn>) {
  tiles.map { tile -> this[tile].setImageResource(turns[tile].tileDrawable) }
}

private operator fun Fragment.get(tile: Tile): ImageView =
  when (tile) {
    Tile.At00 -> tile_00
    Tile.At01 -> tile_01
    Tile.At02 -> tile_02
    Tile.At10 -> tile_10
    Tile.At11 -> tile_11
    Tile.At12 -> tile_12
    Tile.At20 -> tile_20
    Tile.At21 -> tile_21
    Tile.At22 -> tile_22
  }

private val Turn?.tileDrawable get() = this?.player?.drawableRes ?: 0

private operator fun List<Turn>.get(tile: Tile): Turn? = find { it.tile == tile }

private val tiles = Tile.values()

private val Playing.nextTurnPlayer
  get() = turns.lastOrNull()?.player?.opponent ?: Circle

@get:DrawableRes
private val Player.drawableRes: Int
  get() = when (this) {
    Circle -> R.drawable.ic_circle
    Cross -> R.drawable.ic_cross
  }

private val Player.printName
  get() = when (this) {
    Circle -> "◯"
    Cross -> "⨉"
  }
