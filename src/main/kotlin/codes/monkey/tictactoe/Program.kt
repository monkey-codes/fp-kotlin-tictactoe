package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.raise.either

fun program(game: Either<GameError, Game>): IO<Unit> =
  IO.unit(game)
    .flatMap { eg -> IOs.stdout(generateScreen(eg)).map { eg } }
    .flatMap { eg ->
      IOs.stdin().map {
        val (symbol, row, col) = it.split(",")
        either {
          val s = symbol.toSymbol().bind()
          val r = row.toIntOrNull() ?: this.raise(InvalidSymbol(row))
          val c = col.toIntOrNull() ?: this.raise(InvalidSymbol(col))
          val move = Move.move(r, c, s).bind()
          val g = eg.bind()
          val newGame =
            when (g) {
              is InProgress -> g.make(move).bind()
              else -> g
            }
          newGame
        }
      }
    }
    .flatMap { eg2 -> program(eg2) }

fun main() {
  val p = program(Game.new())
  Interpreter.run(p)
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
