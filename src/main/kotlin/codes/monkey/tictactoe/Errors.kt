package codes.monkey.tictactoe

sealed class GameError

data class InvalidSymbol(val value: String) : GameError()

data class NotPlayersTurn(val value: Symbol) : GameError()

data class GameNotInProgress(val game: Game) : GameError()

data class InvalidMoveNumber(val number: String) : GameError()

data class InvalidCoordinates(val row: String, val col: String) : GameError() {
  constructor(row: Int, col: Int) : this(row.toString(), col.toString())
}

data class MoveParseFailure(val input: String) : GameError()
