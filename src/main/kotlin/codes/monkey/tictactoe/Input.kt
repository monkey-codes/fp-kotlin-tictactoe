package codes.monkey.tictactoe

import arrow.core.raise.either

object Input {

  fun io(programState: ProgramState): IO<ProgramState> =
    IOs.stdin().map { handle(programState, it) }

  private fun handle(programState: ProgramState, input: String): ProgramState = either {
    val currentState = programState.bind()
    val errorToCurrentState = { error: GameError -> currentState to error }

    val move = Move.parse(input).mapLeft(errorToCurrentState).bind()
    val newState =
      when (currentState) {
        is InProgress -> currentState.make(move).mapLeft(errorToCurrentState).bind()
        else -> currentState
      }
    newState
  }
}
