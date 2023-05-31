package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right

typealias ProgramState = Either<Pair<Game, GameError>, Game>

fun program(programState: ProgramState): IO<Unit> =
  IOs.gameScreen(programState)
    .flatMap { currentState -> IOs.gameInput(currentState) }
    .flatMap { currentState -> program(currentState) }

fun main() {
  either {
    val game = Game.new().bind()
    val p = program(game.right())
    Interpreter.run(p)
  }
}
