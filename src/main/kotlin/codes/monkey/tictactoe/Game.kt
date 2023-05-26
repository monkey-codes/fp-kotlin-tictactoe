package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right

sealed class GameError

data class InvalidSymbol(val value: String) : GameError()

data class InvalidCoordinates(val row: Row, val col: Col) : GameError()

typealias Cell = Triple<Int, Int, Symbol>

@JvmInline value class Row(val r: Int)

@JvmInline value class Col(val c: Int)

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

typealias Move = Triple<Symbol, Row, Col>

class Game(val state: List<List<Symbol>>) {

  fun make(move: Move): Either<GameError, Game> {
    val (symbol, r, c) = move
    return state
      .mapIndexed { ri, row ->
        if (Row(ri) == r) {
          row.mapIndexed { ci, cell -> if (Col(ci) == c) symbol else cell }
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

fun Game.cell(r: Row, c: Col): Either<InvalidCoordinates, Symbol> {
  val validRange = (0..2)

  if (!validRange.contains(r.r) || !validRange.contains(c.c)) return InvalidCoordinates(r, c).left()
  return state[r.r][c.c].right()
}
