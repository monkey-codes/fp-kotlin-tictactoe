package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right

sealed class GameError

data class InvalidSymbol(val value: String) : GameError()

data class InvalidCoordinates(val coord: Coord) : GameError()

typealias Cell = Triple<Int, Int, Symbol>

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

data class Coord(val row: Int, val col: Int)

data class Move(val coord: Coord, val symbol: Symbol) {
  companion object {
    fun move(r: Int, c: Int, s: Symbol) = Move(Coord(r, c), s)
    fun moves(vararg moves: Move): List<Move> = moves.toList()
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
  get() =
    state.mapIndexed { r, row -> row.mapIndexed { c, symbol -> Cell(r, c, symbol) } }.flatten()

fun Game.cell(coord: Coord): Either<InvalidCoordinates, Symbol> {
  val (row, col) = coord
  val validRange = (0..2)

  if (!validRange.contains(row) || !validRange.contains(col))
    return InvalidCoordinates(coord).left()
  return state[row][col].right()
}
