package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import codes.monkey.tictactoe.Symbol.NOUGHT
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain

class ScreenSpec :
  StringSpec({
    "should handle in progress games" {
      either {
        val (_, context) = simulateGame().bind()
        context shouldMatchScreen
          {
            """
                   |   -    -    -
                   |   -    -    -
                   |   -    -    -
                   |
                   |move for CROSS (eg: 0 0) :
                """
          }
      }
    }

    "should handle won games" {
      either {
        val (_, context) =
          simulateGame { """
            |x0 o1 __
            |x6 o3 o5
            |x2 __ x4""" }
            .bind()

        context shouldMatchScreen
          {
            """
                   |   x    o    -
                   |   x    o    o
                   |   x    -    x
                   |x Won
                   |Ctrl-C to quit or enter for a new game:
                """
          }
      }
    }

    "should handle drawn games" {
      either {
        val (_, context) =
          simulateGame { """
            |x0 o1 x2
            |o3 x4 x6
            |o7 x8 o5""" }
            .bind()

        context shouldMatchScreen
          {
            """
                   |   x    o    x
                   |   o    x    x
                   |   o    x    o
                   |Draw
                   |Ctrl-C to quit or enter for a new game:
                """
          }
      }
    }

    "should handle error states" {
      either {
        val programState = Pair(Game.new().bind(), NotPlayersTurn(NOUGHT))
        val io = IOs.gameScreen(programState.left())
        val (_, context) = interpret(io)
        context shouldMatchScreen
          {
            """
                   |   -    -    -
                   |   -    -    -
                   |   -    -    -
                   |NotPlayersTurn
                   |move for CROSS (eg: 0 0) :
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

fun simulateGame(
  f: () -> String? = { null }
): Either<GameError, Pair<ProgramState, TestInterpreterContext>> {
  return either {
    val moves = f()?.toMoveList()?.bind().orEmpty()
    val game = Game.new().bind().make(moves).bind()
    val io = IOs.gameScreen(game.right())
    interpret(io)
  }
}
