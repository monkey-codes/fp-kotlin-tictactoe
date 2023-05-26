package codes.monkey.tictactoe

import arrow.core.flatten
import arrow.core.raise.either
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
      listOf(
        Move(CROSS, Row(0), Col(0)),
        Move(NOUGHT, Row(0), Col(1)),
        Move(CROSS, Row(2), Col(0)),
        Move(NOUGHT, Row(1), Col(1)),
        Move(CROSS, Row(2), Col(2)),
        Move(NOUGHT, Row(1), Col(2)),
        Move(CROSS, Row(1), Col(0))
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
          val (symbol, r, c) = it

          val value = game.cell(r, c).shouldBeRight()
          value shouldBe symbol
        }
      }
    }
  })
