package codes.monkey.tictactoe

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.checkAll

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
      game.state shouldHaveSize 3
      game.state[0] shouldHaveSize 3
    }

    "should detect won games" {
      winningMoves
        .map { games ->
          checkAll(games) { moves ->
            either {
                val game = Game.new().bind().make(moves).bind()
                moves.forEach { (coord, symbol) -> game.cell(coord) shouldBe symbol }
                game.shouldBeTypeOf<Won>()
              }
              .shouldBeRight()
          }
        }
        .shouldBeRight()
    }
    "should detect drawn games" {
      drawMoves
        .map { games ->
          checkAll(games) { moves ->
            either {
                val game = Game.new().bind().make(moves).bind()
                moves.forEach { (coord, symbol) -> game.cell(coord) shouldBe symbol }
                game.shouldBeTypeOf<Draw>()
              }
              .shouldBeRight()
          }
        }
        .shouldBeRight()
    }
  })
