package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import arrow.core.zip
import codes.monkey.tictactoe.Symbol.BLANK
import codes.monkey.tictactoe.Symbol.CROSS
import codes.monkey.tictactoe.Symbol.NOUGHT

data class Coord private constructor(val row: Int, val col: Int) {
  companion object {
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

private val State.rows: List<List<Symbol>>
  get() = this
private val State.columns: List<List<Symbol>>
  get() = this[0].zip(this[1], this[2]) { a, b, c -> listOf(a, b, c) }
private val State.diagonals: List<List<Symbol>>
  get() =
    listOf(listOf(this[0][0], this[1][1], this[2][2]), listOf(this[0][2], this[1][1], this[2][0]))

private val State.winningConditions
  get() = rows + columns + diagonals

private val State.isWon
  get() =
    winningConditions.any {
      it.all { symbol -> symbol == CROSS } || it.all { symbol -> symbol == NOUGHT }
    }

private val State.isDrawn
  get() = this.all { it.none { symbol -> symbol == BLANK } }

fun State.toGame(): Game =
  when {
    isWon -> Won(this)
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
        .map { game(it.chunked(CELLS_PER_ROW)) }
    }

    fun game(state: State): Game {
      return InProgress(state)
    }

    fun new(): Either<GameError, InProgress> = either {
      when (
        val game =
          of(
              """
            _ _ _
            _ _ _
            _ _ _
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

  fun make(move: Move): Either<GameError, Game> {
    val (coord, symbol) = move
    if (symbol != nextPlayer) return NotPlayersTurn(symbol).left()
    return state
      .mapIndexed { ri, row ->
        if (ri == coord.row) {
          row.mapIndexed { ci, cell -> if (ci == coord.col) symbol else cell }
        } else row
      }
      .toGame()
      .right()
  }
}

class Draw(state: State) : Game(state)

class Won(state: State) : Game(state)

fun Game.cell(coord: Coord): Symbol = this.state[coord.row][coord.col]
