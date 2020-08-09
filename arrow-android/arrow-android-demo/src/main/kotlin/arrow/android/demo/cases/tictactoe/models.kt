package arrow.android.demo.cases.tictactoe

sealed class Event {
    data class TileSelected(val tile: Tile) : Event()
    object ResetGame : Event()
}

sealed class State {
    data class Playing(val turns: List<Turn> = emptyList()) : State()
    data class Win(val player: Player, val turns: List<Turn>) : State()
    data class Tie(val turns: List<Turn>) : State()
}

enum class Tile {
    At00, At01, At02, At10, At11, At12, At20, At21, At22;
}

enum class Player {
    Circle, Cross
}

val Player.opponent: Player
    get() = when (this) {
        Player.Circle -> Player.Cross
        Player.Cross -> Player.Circle
    }

data class Turn(val player: Player, val tile: Tile)
