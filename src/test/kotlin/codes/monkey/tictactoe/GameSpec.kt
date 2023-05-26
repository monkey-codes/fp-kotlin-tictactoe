package codes.monkey.tictactoe

import arrow.core.flatten
import arrow.core.raise.either
import codes.monkey.tictactoe.Move.Companion.move
import codes.monkey.tictactoe.Move.Companion.moves
import codes.monkey.tictactoe.Symbol.CROSS
import codes.monkey.tictactoe.Symbol.NOUGHT
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive

val winningMoves =
  listOf(
      moves(
        move(0, 0, CROSS),
        move(0, 0, CROSS),
        move(0, 1, NOUGHT),
        move(2, 0, CROSS),
        move(1, 1, NOUGHT),
        move(2, 2, CROSS),
        move(1, 2, NOUGHT),
        move(1, 0, CROSS)
      )
    )
    .exhaustive()

class GameSpec :
  StringSpec({
    "should setup game based on string state" {
      val game =
        Game.of(
            """
            _ _ _
            _ _ _
            _ _ _
        """
              .trimIndent()
          )
          .shouldBeRight()
      game.cells shouldHaveSize 9
    }

    "should allow making moves" {
      checkAll(winningMoves) { moves ->
        val game =
          moves
            .fold(Game.new()) { game, move -> either { game.bind().make(move) }.flatten() }
            .shouldBeRight()
        moves.forEach {
          val (coord, symbol) = it

          val value = game.cell(coord).shouldBeRight()
          value shouldBe symbol
        }
      }
    }
  })
