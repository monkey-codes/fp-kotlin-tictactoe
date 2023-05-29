package codes.monkey.tictactoe

sealed class GameError

data class InvalidSymbol(val value: String) : GameError()

data class GameNotInProgress(val game: Game) : GameError()

data class InvalidMoveNumber(val number: String) : GameError()

data class InvalidCoordinates(val row: Int, val col: Int) : GameError()
