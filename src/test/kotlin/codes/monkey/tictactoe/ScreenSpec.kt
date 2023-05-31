package codes.monkey.tictactoe

import arrow.core.raise.either
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain

// fun <Error> Raise<Error>.simulateGame(f: () -> String): Pair<ProgramState,
// TestInterpreterContext> = TODO()

class ScreenSpec :
  StringSpec({
    "should handle in progress games" {
      either {
        val game = Game.new().bind()
        val io = IOs.gameScreen(game.right())
        val (result, context) = interpret(io)
        result.shouldBeRight()
        context shouldMatchScreen
          {
            """
                   |   _    _    _
                   |   _    _    _
                   |   _    _    _
                   |
                   |move for CROSS (eg: 0 0) :
                """
          }
      }
    }

    "should handle won games" {
      either {
        val moves =
          """
            |x0 o1 __
            |x6 o3 o5
            |x2 __ x4"""
            .toMoveList()
            .bind()
        val game = Game.new().bind().make(moves).bind()
        val io = IOs.gameScreen(game.right())
        val (result, context) = interpret(io)
        result.shouldBeRight()
        context shouldMatchScreen
          {
            """
                   |   x    o    _
                   |   x    o    o
                   |   x    _    x
                   |x Won
                   |q to quit or enter for a new game:
                """
          }
      }
    }
  })

fun <A> interpret(io: IO<A>, vararg inputValues: String): Pair<A, TestInterpreterContext> =
  with(TestInterpreterContext(*inputValues)) { Interpreter.run(io, this) to this }

infix fun TestInterpreterContext.shouldMatchScreen(f: () -> String) {
  this.outputs shouldHaveSize 1
  val expected = f().trimIndent().trimMargin()
  outputs.first() shouldContain expected
}
