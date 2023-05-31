package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import codes.monkey.tictactoe.Symbol.CROSS
import codes.monkey.tictactoe.Symbol.NOUGHT

enum class Symbol(val value: String) {
  NOUGHT("o"),
  CROSS("x"),
  BLANK("-")
}

fun String.toSymbol(): Either<InvalidSymbol, Symbol> =
  when (this.lowercase()) {
    "x" -> CROSS.right()
    "o" -> NOUGHT.right()
    "-" -> Symbol.BLANK.right()
    else -> InvalidSymbol(this).left()
  }
