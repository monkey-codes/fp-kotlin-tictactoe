package codes.monkey.tictactoe

import arrow.core.Either
import codes.monkey.tictactoe.Screen.inputPrompt

object Screen {
  private val inputPrompt = listOf("move:")
  //  private val noError = listOf("")

  fun generateScreen(e: Either<Pair<Game, GameError>, Game>): String {
    return when (e) {
      is Either.Left -> generate(e.value.first, e.value.second)
      is Either.Right -> generate(e.value)
    }
  }

  @Suppress("detekt:MagicNumber")
  private fun clearLines(s: String): String {
    val escape = "\u001B"
    val eraseLine = "${escape}[1A${escape}[2K\r"
    //    println(s.lines().size)
    return (1..s.lines().size).map { eraseLine }.joinToString("") + s
  }

  @Suppress("detekt:MagicNumber")
  private fun generate(value: Game, error: GameError? = null): String {

    val margin = "   "
    val screenContent =
      value.state.map { it.joinToString(" ") { symbol -> margin + symbol.value } } +
        (error?.javaClass?.simpleName.orEmpty()) +
        inputPrompt(value)
    return clearLines(screenContent.joinToString("\n"))
  }

  private fun inputPrompt(game: Game): String =
    when (game) {
      is InProgress -> "move for ${game.nextPlayer} (eg: 0 0) :"
      else -> ""
    }
}
