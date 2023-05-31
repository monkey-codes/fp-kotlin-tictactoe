package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import arrow.core.raise.ensure
import codes.monkey.tictactoe.Symbol.BLANK
import codes.monkey.tictactoe.Symbol.CROSS
import codes.monkey.tictactoe.Symbol.NOUGHT

typealias Cell = Pair<Coord, Symbol>

data class Coord private constructor(val row: Int, val col: Int) {
  companion object {

    val rows = (0..2).map { r -> (0..2).map { c -> Coord(r, c) } }
    val columns = List(3) { c -> List(3) { r -> rows[r][c] } }
    val diagonals =
      listOf(0, 0, 1, 1, 2, 2, 0, 2, 1, 1, 2, 0).chunked(2).map { (r, c) -> Coord(r, c) }.chunked(3)

    operator fun invoke(row: Int, col: Int): Either<InvalidCoordinates, Coord> = either {
      val validRange = (0..2)
      ensure(validRange.contains(row) && validRange.contains(col)) { InvalidCoordinates(row, col) }
      Coord(row, col)
    }

    operator fun invoke(row: String, col: String): Either<InvalidCoordinates, Coord> = either {
      val r = row.toIntOrNull() ?: this.raise(InvalidCoordinates(row, col))
      val c = col.toIntOrNull() ?: this.raise(InvalidCoordinates(row, col))
      invoke(r, c).bind()
    }
  }
}

data class Move(val coord: Coord, val symbol: Symbol) {
  companion object {
    private const val REQUIRED_ARGS_SIZE = 3

    @Suppress("detekt:MemberNameEqualsClassName")
    fun move(r: Int, c: Int, s: Symbol): Either<InvalidCoordinates, Move> = either {
      Move(Coord(r, c).bind(), s)
    }

    fun parse(input: String): Either<GameError, Move> = either {
      val arguments = input.trim().split(" ")
      ensure(arguments.size == REQUIRED_ARGS_SIZE) { raise(MoveParseFailure(input)) }
      val (s, row, col) = arguments
      val coord = Coord(row, col).bind()
      Move(coord, s.toSymbol().bind())
    }
  }
}

typealias State = List<List<Symbol>>

private val State.rows: List<List<Cell>>
  get() = Coord.rows.map { values(it) }
private val State.columns: List<List<Cell>>
  get() = Coord.columns.map { values(it) }
private val State.diagonals: List<List<Cell>>
  get() = Coord.diagonals.map { values(it) }

private val State.winningConditions
  get() = rows + columns + diagonals

private val State.winningCoordinates: List<Cell>
  get() =
    winningConditions
      .firstOrNull {
        it.all { (_, symbol) -> symbol == CROSS } || it.all { (_, symbol) -> symbol == NOUGHT }
      }
      .orEmpty()
private val State.isWon
  get() = winningCoordinates.isNotEmpty()

private val State.isDrawn
  get() = this.all { it.none { symbol -> symbol == BLANK } }

fun State.values(coords: List<Coord>): List<Cell> =
  coords.map { coord -> coord to this[coord.row][coord.col] }

fun State.toGame(): Game =
  when {
    isWon -> Won(this, winningCoordinates.first().second)
    isDrawn -> Draw(this)
    else -> InProgress(this)
  }

sealed class Game(val state: State) {

  companion object {
    private const val CELLS_PER_ROW = 3

    fun of(state: String): Either<GameError, Game> {
      return state
        .trim()
        .split(" ", "\n")
        .let { /*traverse*/ l -> either { l.map { it.toSymbol().bind() } } }
        .map { it.chunked(CELLS_PER_ROW).toGame() }
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
            is InProgress -> game.make(move)
            else -> this.raise(GameNotInProgress(eg.bind()))
          }
        }
        .flatten()
    }

  fun make(move: Move): Either<GameError, Game> = either {
    val (coord, symbol) = move
    ensure(symbol == nextPlayer) { raise(NotPlayersTurn(symbol)) }
    ensure(cell(move.coord) == BLANK) { raise(InvalidMove(move)) }

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

fun Game.cell(coord: Coord): Symbol = this.state[coord.row][coord.col]
