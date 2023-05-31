package codes.monkey.tictactoe

import arrow.core.raise.either

object Input {

  fun io(programState: ProgramState): IO<ProgramState> =
    IOs.stdin().map { handle(programState, it) }

  private fun handle(programState: ProgramState, input: String): ProgramState = either {
    val currentState = programState.bind()
    val errorToCurrentState = { error: GameError -> currentState to error }
    if (input.isEmpty()) return Game.new().mapLeft(errorToCurrentState)

    val newState =
      when (currentState) {
        is InProgress -> {
          val move =
            Move.parse("${currentState.nextPlayer.value} $input")
              .mapLeft(errorToCurrentState)
              .bind()
          currentState.make(move).mapLeft(errorToCurrentState).bind()
        }
        else -> currentState
      }
    newState
  }
}
