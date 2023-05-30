package codes.monkey.tictactoe

import arrow.core.raise.either
import arrow.core.right
import codes.monkey.tictactoe.Move.Companion.move
import codes.monkey.tictactoe.Symbol.CROSS
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class InputSpec :
  StringSpec({
    "should handle valid input" {
      either {
        val game = Game.new().bind()
        val io = IOs.gameInput(game.right())
        val result = with(TestInterpreterContext("x 0 0")) { Interpreter.run(io, this) }
        result.shouldBeRight() shouldBeEqual game.make(move(0, 0, CROSS).bind()).bind()
      }
    }

    "should handle invalid input" {
      either {
        val game = Game.new().bind()
        val io = IOs.gameInput(game.right())
        val result = with(TestInterpreterContext("x 9 10")) { Interpreter.run(io, this) }
        result.shouldBeLeft() shouldBeEqual (game to InvalidCoordinates(9, 10))
      }
    }
  })
