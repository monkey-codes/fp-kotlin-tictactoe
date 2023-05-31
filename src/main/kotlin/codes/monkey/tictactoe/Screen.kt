package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.right

object Screen {
  private fun ProgramState.toMostRecentValidState() =
    when (this) {
      is Either.Left -> value.first.right()
      else -> this
    }

  fun io(programState: ProgramState): IO<ProgramState> =
    Suspend { generate(programState) }
      .flatMap { IOs.stdout(it).map { programState.toMostRecentValidState() } }

  fun generate(e: Either<Pair<Game, GameError>, Game>): String {
    return when (e) {
      is Either.Left -> generate(e.value.first, e.value.second)
      is Either.Right -> generate(e.value)
    }
  }

  @Suppress("detekt:MagicNumber")
  private fun clearLines(s: String): String {
    val escape = "\u001B"
    val eraseLine = "${escape}[1A${escape}[2K\r"
    return (1..s.lines().size).map { eraseLine }.joinToString("") + s
  }

  @Suppress("detekt:MagicNumber")
  private fun generate(value: Game, error: GameError? = null): String {
    val margin = "   "
    val screenContent =
      value.state.map { it.joinToString(" ") { symbol -> margin + symbol.value } } +
        (message(value, error)) +
        inputPrompt(value)
    return clearLines(screenContent.joinToString("\n"))
  }

  private fun message(game: Game, error: GameError? = null): String {
    return error?.javaClass?.simpleName
      ?: when (game) {
        is Won -> "${game.winner.value} Won"
        else -> ""
      }
  }

  private fun inputPrompt(game: Game): String =
    when (game) {
      is InProgress -> "move for ${game.nextPlayer} (eg: 0 0) :"
      else -> "q to quit or enter for a new game:"
    }
}
