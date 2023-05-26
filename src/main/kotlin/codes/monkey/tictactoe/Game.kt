package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import java.lang.IllegalStateException

sealed class GameError

data class InvalidSymbol(val value: String) : GameError()

data class InvalidCoordinates(val row: Int, val col: Int) : GameError()

typealias Cell = Pair<Coord, Symbol>

enum class Symbol(val value: String) {
  NOUGHT("o"),
  CROSS("x"),
  BLANK("_")
}

fun String.toSymbol(): Either<InvalidSymbol, Symbol> =
  when (this.lowercase()) {
    "x" -> Symbol.CROSS.right()
    "o" -> Symbol.NOUGHT.right()
    "_" -> Symbol.BLANK.right()
    else -> InvalidSymbol(this).left()
  }

data class Coord private constructor (val row: Int, val col: Int) {
  companion object {
    operator fun invoke(row: Int, col: Int): Either<InvalidCoordinates, Coord> = either {
      val validRange = (0..2)
      ensure(validRange.contains(row) && validRange.contains(col)) { InvalidCoordinates(row, col) }
      Coord(row, col)
    }
  }
}

data class Move(val coord: Coord, val symbol: Symbol) {
  companion object {
    fun move(r: Int, c: Int, s: Symbol) = either {
      Move(Coord(r, c).bind(), s)
    }

    fun moves(vararg moves: Either<InvalidCoordinates,Move>): List<Either<InvalidCoordinates,Move>> = moves.toList()
  }
}

class Game(val state: List<List<Symbol>>) {

  fun make(move: Move): Either<GameError, Game> {
    val (coord, symbol) = move
    return state
      .mapIndexed { ri, row ->
        if (ri == coord.row) {
          row.mapIndexed { ci, cell -> if (ci == coord.col) symbol else cell }
        } else row
      }
      .let { Game(it).right() }
  }

  companion object {
    private const val CELLS_PER_ROW = 3

    fun of(state: String): Either<GameError, Game> {
      return state
        .trim()
        .split(" ", "\n")
        .let { /*traverse*/ l -> either { l.map { it.toSymbol().bind() } } }
        .map { Game(it.chunked(CELLS_PER_ROW)) }
    }

    fun new() =
      of(
        """
            _ _ _
            _ _ _
            _ _ _
        """
          .trimIndent()
      )
  }
}

val Game.cells: List<Cell>
  get() = either {
    state.mapIndexed { r, row -> row.mapIndexed { c, symbol -> Cell(Coord(r,c).bind(), symbol) } }.flatten()
  }.getOrElse { throw IllegalStateException() }

fun Game.cell(coord: Coord): Either<InvalidCoordinates, Symbol>  = either {
  val (row, col) = coord
  return state[row][col].right()
}
