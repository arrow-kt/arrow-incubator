package arrow.android.demo

enum class DemoCase(val title: String) {

  Clicks("Clicks"),
  Login("Login"),
  GameOfSelection("Game Of Selection"),
  TicTacToe("Tic-Tac-Toe");

  companion object {
    private val demos = DemoCase::class.java.enumConstants!!
    operator fun get(index: Int): DemoCase = demos[index]
    val size = demos.size
  }

}
