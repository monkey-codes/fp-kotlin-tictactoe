package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.raise.either
import codes.monkey.tictactoe.Symbol.BLANK
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.exhaustive

fun String.toMoveList(): Either<GameError, List<Move>> =
  either {
      trimIndent()
        .trim()
        .split("\n", " ")
        .chunked(3)
        .flatMapIndexed { ri, row ->
          row.flatMapIndexed { ci, str ->
            val symbol = str[0].toString().toSymbol().bind()
            if (symbol != BLANK) {
              val moveNumber = str[1].digitToIntOrNull() ?: raise(InvalidMoveNumber(str))
              listOf(moveNumber to Move.move(ri, ci, symbol))
            } else emptyList()
          }
        }
        .sortedBy { it.first }
        .map { it.second }
    }
    .let { /*traverse*/ l -> either { l.bind().map { it.bind() } } }

val winningMoves: Either<GameError, Exhaustive<List<Move>>> = either {
  listOf(
      """
        x0 o1 __
        x6 o3 o5
        x2 __ x4
      """.toMoveList().bind(),
      """
       x0 o1 x2
       x6 o3 __
       x4 __ o5
      """.toMoveList().bind(),
      """
       x0 x2 x4
       o1 o3 __
       __ __ __
      """.toMoveList().bind(),
      """
       x0 o1 o3
       __ x2 __
       __ __ x4
      """.toMoveList().bind(),
      """
       o3 o1 x0
       __ x2 __
       x4 __ __
      """.toMoveList().bind(),
    )
    .exhaustive()
}

val drawMoves: Either<GameError, Exhaustive<List<Move>>> = either {
  listOf(
      """
    x0 o1 x2
    x4 x6 o3
    o7 x8 o5
    """.toMoveList().bind(),
    )
    .exhaustive()
}
