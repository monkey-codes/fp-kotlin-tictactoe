package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.raise.either
import codes.monkey.tictactoe.Symbol.CROSS
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.map

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

    "should keep track of next player" {
      val inProgressMoves = winningMoves.map { game -> game.map { moves -> moves.dropLast(1) } }
      inProgressMoves.map { games ->
        checkAll(games) { moves ->
          either {
              moves.fold(Game.new() as Either<GameError, Game>) { game, move ->
                when (val g = game.bind()) {
                  is InProgress -> {
                    g.nextPlayer shouldBe move.symbol
                    g.make(move)
                  }
                  else -> raise(GameNotInProgress(g))
                }
              }
            }
            .shouldBeRight()
        }
      }
    }

    "should not allow the same player to make 2 moves in a row" {
      val oneSided =
        winningMoves.map { game ->
          game.map { moves -> moves.map { move -> move.copy(symbol = CROSS) } }
        }
      oneSided.map { games ->
        checkAll(games) { moves -> either { Game.new().bind().make(moves).bind() }.shouldBeLeft() }
      }
    }
    "should detect won games" {
      winningMoves
        .map { games ->
          checkAll(games) { moves ->
            either {
                val game = Game.new().bind().make(moves).bind()
                moves.forEach { (coord, symbol) -> game.cell(coord) shouldBe symbol }
                val won = game.shouldBeTypeOf<Won>()
                won.winner shouldBe CROSS
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
