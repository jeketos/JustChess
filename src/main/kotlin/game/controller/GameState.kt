package game.controller

import game.data.*

data class GameState(
    val board: Board = Board(),
    val selectedCell: Cell? = null,
    val movePossibilities: List<FigureMoving>? = null,
    val turn: GameColor = GameColor.White,
    val gameCondition: GameCondition = GameCondition.NothingSpecial,
    val moveCount: Int = 0
)
