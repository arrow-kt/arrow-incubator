package arrow.android.demo.cases.tictactoe

import androidx.lifecycle.ViewModel
import arrow.android.demo.cases.login.StateMachine
import arrow.fx.coroutines.stream.Stream

class TicTacToeViewModel : ViewModel() {

  private val stateMachine = StateMachine<State, Event>(State.Playing()) { s, e -> Stream(s.transition(e)) }

  suspend fun compute(events: Stream<Event>): Stream<State> = stateMachine.start(events)

}

private fun State.transition(event: Event): State = when {
  event is Event.TileSelected && this is State.Playing && event.tile !in tiles -> {
    val newTurns = turns + when {
      turns.isEmpty() -> Turn(Player.Circle, event.tile)
      else -> Turn(turns.last().player.opponent, event.tile)
    }

    val winner = newTurns.findWinner()

    when {
      winner != null -> State.Win(winner, newTurns)
      newTurns.isTide() -> State.Tie(newTurns)
      else -> copy(turns = newTurns)
    }
  }
  event is Event.ResetGame -> State.Playing()
  else -> this
}

private val State.Playing.tiles: List<Tile> get() = turns.map { it.tile }

private fun List<Turn>.findWinner() =
  winingCandidates.mapNotNull { it.findWinnerIn(this) }.firstOrNull()

private fun List<Turn>.isTide(): Boolean = size == 9

private fun State.Playing.asTide(): State? =
  turns.takeIf { it.size == 9 }?.let(State::Tie)

private val winingCandidates = listOf(
  listOf(Tile.At00, Tile.At01, Tile.At02),
  listOf(Tile.At10, Tile.At11, Tile.At12),
  listOf(Tile.At20, Tile.At21, Tile.At22),
  listOf(Tile.At00, Tile.At10, Tile.At20),
  listOf(Tile.At01, Tile.At11, Tile.At21),
  listOf(Tile.At02, Tile.At12, Tile.At22),
  listOf(Tile.At00, Tile.At11, Tile.At22),
  listOf(Tile.At02, Tile.At11, Tile.At20)
)

private fun List<Tile>.findWinnerIn(turns: List<Turn>): Player? =
  mapNotNull { tile -> turns.find { it.tile == tile } }
    .map { it.player }
    .let { players ->
      when (players) {
        listOf(Player.Circle, Player.Circle, Player.Circle) -> Player.Circle
        listOf(Player.Cross, Player.Cross, Player.Cross) -> Player.Cross
        else -> null
      }
    }
