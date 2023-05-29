package codes.monkey.tictactoe

import arrow.core.Either
import arrow.core.left
import arrow.core.right

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
