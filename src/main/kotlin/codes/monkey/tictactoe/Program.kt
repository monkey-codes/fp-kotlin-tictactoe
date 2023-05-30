package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right

fun Either<Pair<Game, GameError>, Game>.toMostRecentValidState() =
  when (this) {
    is Either.Left -> value.first.right()
    else -> this
  }

fun program(game: Either<Pair<Game, GameError>, Game>): IO<Unit> =
  IO.unit(game)
    .flatMap { eg -> IOs.stdout(Screen.generateScreen(eg)).map { eg.toMostRecentValidState() } }
    .flatMap { eg ->
      IOs.stdin().map { input ->
        either {
          val currentState = eg.bind()
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
    }
    .flatMap { eg2 -> program(eg2) }

fun main() {
  either {
    val game = Game.new().bind()
    val p = program(game.right())
    Interpreter.run(p)
  }
}

fun main2() {
  val black = "\u001b[30m"
  val red = "\u001b[31m"
  val green = "\u001b[32m"
  val yellow = "\u001b[33m"
  val blue = "\u001b[34m"
  val purple = "\u001b[35m"
  val cyan = "\u001b[36m"
  val white = "\u001b[37m"
  val reset = "\u001B[0m"
  //  val clearS = "\u001B[2J"
  //  val clearS = "\u001B[H"
  //  println(clearS)
  println(red + "Text string in red" + reset)
  println(green + "Text string in green" + reset)
  println(yellow + "Text string in yellow" + reset)
  println(white + "Text string in white" + reset)
  println(
    black +
      "Black text string" +
      reset +
      "(<- black text string that cannot be seen because my background is black)" +
      reset
  )
  println(blue + "Text string in blue" + reset)
  println(purple + "Magenta string" + reset)
  println(cyan + "Text string in cyan" + reset)
  println(reset + "Default color string" + reset)
  //  println("hello"+clearS+"bye")
  //  println()
  println("hello")
  println("hello")
  println("hello")
  val escape = "\u001B"
  val eraseLine = "${escape}[1A${escape}[2K\r"
  println(eraseLine + "bye")
  //  println(clearS + "hello")
}
