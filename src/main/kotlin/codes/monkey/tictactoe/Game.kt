package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import arrow.core.raise.ensure
import codes.monkey.tictactoe.Coordinates.Companion.columns
import codes.monkey.tictactoe.Coordinates.Companion.diagonals
import codes.monkey.tictactoe.Coordinates.Companion.rows
import codes.monkey.tictactoe.Symbol.BLANK
import codes.monkey.tictactoe.Symbol.CROSS
import codes.monkey.tictactoe.Symbol.NOUGHT

typealias Cell = Pair<Coordinates, Symbol>

fun List<Cell>.allSymbolsMatch(s: Symbol): Boolean = all { (_, symbol) -> symbol == s }

data class Coordinates private constructor(val row: Int, val col: Int) {
  companion object {

    val rows = (0..2).map { r -> (0..2).map { c -> Coordinates(r, c) } }
    val columns = List(3) { c -> List(3) { r -> rows[r][c] } }
    val diagonals =
      listOf(0, 0, 1, 1, 2, 2, 0, 2, 1, 1, 2, 0)
        .chunked(2)
        .map { (r, c) -> Coordinates(r, c) }
        .chunked(3)

    operator fun invoke(row: Int, col: Int): Either<InvalidCoordinates, Coordinates> = either {
      val validRange = (0..2)
      ensure(validRange.contains(row) && validRange.contains(col)) { InvalidCoordinates(row, col) }
      Coordinates(row, col)
    }

    operator fun invoke(row: String, col: String): Either<InvalidCoordinates, Coordinates> =
      either {
        val r = row.toIntOrNull() ?: this.raise(InvalidCoordinates(row, col))
        val c = col.toIntOrNull() ?: this.raise(InvalidCoordinates(row, col))
        invoke(r, c).bind()
      }
  }
}

data class Move(val coordinates: Coordinates, val symbol: Symbol) {
  companion object {
    private const val REQUIRED_ARGS_SIZE = 3

    @Suppress("detekt:MemberNameEqualsClassName")
    fun move(r: Int, c: Int, s: Symbol): Either<InvalidCoordinates, Move> = either {
      Move(Coordinates(r, c).bind(), s)
    }

    fun parse(input: String): Either<GameError, Move> = either {
      val arguments = input.trim().split(" ")
      ensure(arguments.size == REQUIRED_ARGS_SIZE) { raise(MoveParseFailure(input)) }
      val (s, row, col) = arguments
      val coordinates = Coordinates(row, col).bind()
      Move(coordinates, s.toSymbol().bind())
    }
  }
}

typealias State = List<List<Symbol>>

fun State.values(coordinates: List<Coordinates>): List<Cell> =
  coordinates.map { coordinate -> coordinate to this[coordinate.row][coordinate.col] }

fun State.toGame(): Game = Game.of(this)

sealed class Game(val state: State) {

  companion object {
    private const val CELLS_PER_ROW = 3

    fun of(state: State): Game {
      val winningCells =
        (rows + columns + diagonals)
          .map { coordinates -> state.values(coordinates) }
          .firstOrNull { it.allSymbolsMatch(CROSS) || it.allSymbolsMatch(NOUGHT) }
          .orEmpty()
      val isDrawn by lazy { state.all { it.none { symbol -> symbol == BLANK } } }
      return when {
        winningCells.isNotEmpty() -> Won(state, winningCells.first().second)
        isDrawn -> Draw(state)
        else -> InProgress(state)
      }
    }

    fun of(state: String): Either<GameError, Game> {
      return state
        .trim()
        .split(" ", "\n")
        .let { /*traverse*/ l -> either { l.map { it.toSymbol().bind() } } }
        .map { of(it.chunked(CELLS_PER_ROW)) }
    }

    fun new(): Either<GameError, InProgress> = either {
      when (
        val game =
          of(
              """
            - - -
            - - -
            - - -
        """
                .trimIndent()
            )
            .bind()
      ) {
        is InProgress -> game
        else -> raise(GameNotInProgress(game))
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as Game
    return state == other.state
  }

  override fun hashCode(): Int {
    return state.hashCode()
  }
}

class InProgress(state: State) : Game(state) {

  val nextPlayer = if (state.flatten().count { it != BLANK } % 2 == 0) CROSS else NOUGHT

  fun make(moves: List<Move>): Either<GameError, Game> =
    moves.fold(either<GameError, Game> { this@InProgress }) { eg, move ->
      either {
        when (val game = eg.bind()) {
          is InProgress -> game.make(move).bind()
          else -> this.raise(GameNotInProgress(game))
        }
      }
    }

  fun make(move: Move): Either<GameError, Game> = either {
    val (coord, symbol) = move
    ensure(symbol == nextPlayer) { raise(NotPlayersTurn(symbol)) }
    ensure(cell(move.coordinates) == BLANK) { raise(InvalidMove(move)) }

    state
      .mapIndexed { ri, row ->
        if (ri == coord.row) {
          row.mapIndexed { ci, cell -> if (ci == coord.col) symbol else cell }
        } else row
      }
      .toGame()
  }
}

class Draw(state: State) : Game(state)

class Won(state: State, val winner: Symbol) : Game(state)

fun Game.cell(coordinates: Coordinates): Symbol = this.state[coordinates.row][coordinates.col]
