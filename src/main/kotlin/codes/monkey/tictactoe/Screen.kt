package codes.monkey.tictactoe

import arrow.core.Either

fun generateScreen(e: Either<GameError, Game>): String {
  return when (e) {
    is Either.Left -> e.value.toString()
    is Either.Right -> generate(e.value)
  }
}

@Suppress("detekt:MagicNumber")
fun clearLines(s: String): String {
  val escape = "\u001B"
  val eraseLine = "${escape}[1A${escape}[2K\r"
  //    println(s.lines().size)
  return (1..s.lines().size).map { eraseLine }.joinToString("") + s
}

@Suppress("detekt:MagicNumber")
fun generate(value: Game): String {
  val margin = "   "
  val gameState = value.state.map { it.joinToString(" ") { symbol -> margin + symbol.value } }
  val inputPrompt = listOf("", "move:")

  return clearLines((gameState + inputPrompt).joinToString("\n"))
}
