package game.ui.hotseat

import game.controller.GameController
import game.controller.GameState
import game.data.Cell
import game.data.figure.Figure
import kotlinx.coroutines.flow.StateFlow

class HotSeatGameViewModel {

    private val gameController = GameController()
    val gameState: StateFlow<GameState> = gameController.gameState


    fun startNewGame() {
        gameController.startNewGame()
    }

    fun onCellClick(clickedCell: Cell) {
        gameController.onCellClick(clickedCell)
    }

    fun onMutationSelected(selected: Cell, figure: Figure) {
        gameController.onMutationSelected(selected, figure)
    }

}

//class HotSeatGameViewModel {
//
//    val boardState = MutableStateFlow(Board())
//    val turnState = MutableStateFlow(Turn(color = GameColor.White))
//
//    init {
//        startNewGame()
//    }
//
//    private fun initFigures(cell: Cell, color: GameColor): Cell {
//        val figure = when (cell.name) {
//            CellName.A, CellName.H -> Rook(color)
//            CellName.B, CellName.G -> Knight(color)
//            CellName.C, CellName.F -> Bishop(color)
//            CellName.D -> Queen(color)
//            CellName.E -> King(color)
//        }
//        return cell.copy(figure = figure)
//    }
//
//    fun startNewGame() {
//        CoroutineScope(Dispatchers.IO).launch {
//            Client.makeRequest()
//        }
//        val board = Board()
//        boardState.value =
//            board.copy(cells = board.cells.map { column ->
//                column.map { cell ->
//                    when (cell.number) {
//                        CellNumber.N2 -> cell.copy(figure = Pawn(GameColor.White))
//                        CellNumber.N7 -> cell.copy(figure = Pawn(GameColor.Black))
//                        CellNumber.N1 -> initFigures(cell, GameColor.White)
//                        CellNumber.N8 -> initFigures(cell, GameColor.Black)
//                        else -> cell
//                    }
//                }
//            })
//
//        println("board cells ${boardState.value.cellsFlatten}")
//        turnState.update {
//            Turn(color = GameColor.White)
//        }
//    }
//
//    fun onCellClick(clickedCell: Cell) {
//        val selected = boardState.value.selectedCell
//
//        when {
//            clickedCell.figure != null && clickedCell.figure.color != turnState.value.color && selected == null -> {}
//            clickedCell.figure?.color == turnState.value.color -> {
//                update(
//                    selected = clickedCell,
//                    movePossibilities = calculateMovePossibilities(clickedCell.figure, clickedCell, boardState.value)
//                )
//            }
//            selected?.figure != null -> {
//                processFigureMoving(selected.figure, selected, clickedCell)
//            }
//            clickedCell.figure != null -> update(
//                selected = clickedCell,
//                movePossibilities = calculateMovePossibilities(clickedCell.figure, clickedCell, boardState.value)
//            ) // could be removed
//        }
//    }
//
//    private fun calculateMovePossibilities(figure: Figure, clickedCell: Cell, board: Board): List<FigureMoving> {
//        val possibleMoves = figure.calculatePossibleMoves(clickedCell, boardState.value)
//        return possibleMoves.filter { moving ->
//            val updatedBoard = board.update(
//                calculateModifiedCells(
//                    figure = figure,
//                    selected = clickedCell,
//                    clickedCell = moving.cellToMove,
//                    movePossibilities = possibleMoves
//                )
//            )
//            val kingCell = updatedBoard.cellsFlatten.first { it.figure is King && it.figure.color == figure.color }
//            kingCell.isUnderAttack(figure.color, updatedBoard).not()
//        }
//
//    }
//
//    private fun calculateModifiedCells(
//        figure: Figure,
//        selected: Cell,
//        clickedCell: Cell,
//        movePossibilities: List<FigureMoving>?
//    ): List<Cell> {
//        val pawnCellUpdates = movePossibilities
//            ?.filterIsInstance<FigureMoving.Pawn>()
//            ?.firstOrNull()
//            ?.takeIf {
//                it.cellToMove.id == clickedCell.id
//            }
//            ?.attackedCell?.copy(figure = null)
//        val kingCellUpdates = movePossibilities
//            ?.filterIsInstance<FigureMoving.King>()
//            ?.firstOrNull()
//            ?.takeIf {
//                it.cellToMove.id == clickedCell.id
//            }
//            ?.let {
//                val rook = it.rookCell?.figure
//                listOfNotNull(
//                    it.rookCell?.copy(figure = null),
//                    it.castlingCell?.copy(figure = rook)
//                ).toTypedArray()
//            } ?: emptyArray()
//
//        return listOfNotNull(
//            selected.copy(figure = null),
//            clickedCell.copy(figure = figure.copy(figure.moveCount + 1)),
//            pawnCellUpdates,
//            *kingCellUpdates
//        )
//    }
//
//    private fun processFigureMoving(figure: Figure, selected: Cell, clickedCell: Cell) {
//        val movePossibilities = boardState.value.movePossibilities ?: return
//        if (movePossibilities.any { it.cellToMove == clickedCell }) {
//            update(
//                modifiedCells = calculateModifiedCells(
//                    figure = figure,
//                    selected = selected,
//                    clickedCell = clickedCell,
//                    movePossibilities = movePossibilities
//                ),
//                selected = null,
//                movePossibilities = null
//
//            )
//            turnState.update {
//                val color = it.color.toggle()
//                it.copy(
//                    color = color,
//                    gameCondition = getGameCondition(color)
//                )
//            }
//        }
//    }
//
//    private fun getGameCondition(gameColor: GameColor): GameCondition {
//        val board = boardState.value
//        val kingCell = board.cellsFlatten.first { it.figure is King && it.figure.color == gameColor }
//        val underAttack = kingCell.isUnderAttack(gameColor, board)
//        val allPossibleMoves = board.cellsFlatten
//            .filter { it.figure != null && it.figure.color == gameColor }
//            .flatMap {
//                calculateMovePossibilities(it.figure!!, it, board)
//            }
//        val pawnCell = board.cellsFlatten
//            .filter { it.figure is Pawn && it.figure.color == turnState.value.color }
//            .find { it.number == CellNumber.N8 || it.number == CellNumber.N1 }
//        return when {
//            pawnCell != null -> GameCondition.Mutation(pawnCell, turnState.value.color)
//            underAttack && allPossibleMoves.isEmpty() -> GameCondition.Mate
//            allPossibleMoves.isEmpty() -> GameCondition.Stalemate
//            underAttack -> GameCondition.Check
//            else -> GameCondition.NothingSpecial
//        }
//    }
//
//    private fun update(
//        modifiedCells: List<Cell>? = null,
//        selected: Cell? = null,
//        movePossibilities: List<FigureMoving>? = null
//    ) = boardState.update {
//            it.copy(
//                cells = it.cells.map { cells ->
//                    cells.map { cell ->
//                        modifiedCells?.find { modified -> cell.id == modified.id } ?: cell
//                    }
//                },
//                selectedCell = selected,
//                movePossibilities = movePossibilities
//            )
//        }
//
//    fun onMutationSelected(selected: Cell, figure: Figure) {
//        boardState.update {
//            it.copy(
//                cells = it.cells.map { cells ->
//                    cells.map { cell ->
//                        if (cell.id == selected.id) cell.copy(figure = figure)
//                        else cell
//                    }
//                },
//                selectedCell = null,
//                movePossibilities = null
//            )
//        }
//        turnState.update {
//            it.copy(
//                gameCondition = getGameCondition(it.color)
//            )
//        }
//    }
//
//}

