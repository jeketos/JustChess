package game.controller

import game.api.response.Room
import game.api.response.User
import game.data.*
import game.data.figure.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class GameController {

    val gameState = MutableStateFlow(GameState())

    init {
        startNewGame()
    }

    private fun initFigures(cell: Cell, color: GameColor): Cell {
        val figure = when (cell.name) {
            CellName.A, CellName.H -> Rook(color)
            CellName.B, CellName.G -> Knight(color)
            CellName.C, CellName.F -> Bishop(color)
            CellName.D -> Queen(color)
            CellName.E -> King(color)
        }
        return cell.copy(figure = figure)
    }

    fun startNewGame() {
        val board = Board()
        gameState.value =
            GameState(
                board = board.copy(cells = board.cells.map { column ->
                    column.map { cell ->
                        when (cell.number) {
                            CellNumber.N2 -> cell.copy(figure = Pawn(GameColor.White))
                            CellNumber.N7 -> cell.copy(figure = Pawn(GameColor.Black))
                            CellNumber.N1 -> initFigures(cell, GameColor.White)
                            CellNumber.N8 -> initFigures(cell, GameColor.Black)
                            else -> cell
                        }
                    }
                }),
                turn = GameColor.White
            )
    }

    fun onCellClick(clickedCell: Cell) {
        val selected = gameState.value.selectedCell

        when {
            clickedCell.figure != null && clickedCell.figure.color != gameState.value.turn && selected == null -> {}
            clickedCell.figure?.color == gameState.value.turn -> {
                update(
                    selected = clickedCell,
                    movePossibilities = calculateMovePossibilities(
                        clickedCell.figure,
                        clickedCell,
                        gameState.value.board
                    )
                )
            }
            selected?.figure != null -> {
                processFigureMoving(selected.figure, selected, clickedCell)
            }
            clickedCell.figure != null -> update(
                selected = clickedCell,
                movePossibilities = calculateMovePossibilities(clickedCell.figure, clickedCell, gameState.value.board)
            ) // could be removed
        }
    }

    fun onMutationSelected(selected: Cell, figure: Figure) {
        gameState.update {
            val board = it.board.copy(
                cells = it.board.cells.map { cells ->
                    cells.map { cell ->
                        if (cell.id == selected.id) cell.copy(figure = figure)
                        else cell
                    }
                })
            it.copy(
                board = board,
                selectedCell = null,
                movePossibilities = null,
                turn = it.turn.toggle(),
                gameCondition = getGameCondition(board, it.turn.toggle()),
                moveCount = it.moveCount + 1
            )
        }
    }

    fun setStateByRoom(room: Room, user: User, userColor: GameColor) {
        gameState.update {
            val decompressBoard = room.gameState.board.decompressBoard()
            val turn = if (room.gameState.turn == user.uid) userColor else userColor.toggle()
            it.copy(
                board = decompressBoard,
                turn = turn,
                gameCondition = getGameCondition(decompressBoard, turn),
                selectedCell = null,
                movePossibilities = null,
                moveCount = room.gameState.movesCount
            )
        }
    }

    private fun calculateMovePossibilities(figure: Figure, clickedCell: Cell, board: Board): List<FigureMoving> {
        val possibleMoves = figure.calculatePossibleMoves(clickedCell, gameState.value.board)
        return possibleMoves.filter { moving ->
            val updatedBoard = board.update(
                calculateModifiedCells(
                    figure = figure,
                    selected = clickedCell,
                    clickedCell = moving.cellToMove,
                    movePossibilities = possibleMoves
                )
            )
            val kingCell = updatedBoard.cellsFlatten.first { it.figure is King && it.figure.color == figure.color }
            kingCell.isUnderAttack(figure.color, updatedBoard).not()
        }

    }

    private fun calculateModifiedCells(
        figure: Figure,
        selected: Cell,
        clickedCell: Cell,
        movePossibilities: List<FigureMoving>?
    ): List<Cell> {
        val pawnCellUpdates = movePossibilities
            ?.filterIsInstance<FigureMoving.Pawn>()
            ?.firstOrNull()
            ?.takeIf {
                it.cellToMove.id == clickedCell.id
            }
            ?.attackedCell?.copy(figure = null)
        val kingCellUpdates = movePossibilities
            ?.filterIsInstance<FigureMoving.King>()
            ?.firstOrNull()
            ?.takeIf {
                it.cellToMove.id == clickedCell.id
            }
            ?.let {
                val rook = it.rookCell?.figure
                listOfNotNull(
                    it.rookCell?.copy(figure = null),
                    it.castlingCell?.copy(figure = rook)
                ).toTypedArray()
            } ?: emptyArray()

        return listOfNotNull(
            selected.copy(figure = null),
            clickedCell.copy(figure = figure.copy(figure.moveCount + 1)),
            pawnCellUpdates,
            *kingCellUpdates
        )
    }

    private fun processFigureMoving(figure: Figure, selected: Cell, clickedCell: Cell) {
        val movePossibilities = gameState.value.movePossibilities ?: return
        if (movePossibilities.any { it.cellToMove == clickedCell }) {
            val toggle = gameState.value.turn.toggle()
            update(
                modifiedCells = calculateModifiedCells(
                    figure = figure,
                    selected = selected,
                    clickedCell = clickedCell,
                    movePossibilities = movePossibilities
                ),
                selected = null,
                movePossibilities = null,
                turn = toggle,
            )
        }
    }

    private fun getGameCondition(board: Board, gameColor: GameColor): GameCondition {
        val kingCell = board.cellsFlatten.first { it.figure is King && it.figure.color == gameColor }
        val underAttack = kingCell.isUnderAttack(gameColor, board)
        val allPossibleMoves = board.cellsFlatten
            .filter { it.figure != null && it.figure.color == gameColor }
            .flatMap {
                calculateMovePossibilities(it.figure!!, it, board)
            }
        return when {
            underAttack && allPossibleMoves.isEmpty() -> GameCondition.Mate
            allPossibleMoves.isEmpty() -> GameCondition.Stalemate
            underAttack -> GameCondition.Check
            else -> GameCondition.NothingSpecial
        }
    }

    private fun update(
        modifiedCells: List<Cell>? = null,
        selected: Cell? = null,
        movePossibilities: List<FigureMoving>? = null,
        turn: GameColor = gameState.value.turn
    ) = gameState.update {
        val board = it.board.copy(
            cells = it.board.cells.map { cells ->
                cells.map { cell ->
                    modifiedCells?.find { modified -> cell.id == modified.id } ?: cell
                }
            })
        val pawnMutationCell = pawnMutationCell(board)
        it.copy(
            board = board,
            selectedCell = selected,
            movePossibilities = movePossibilities,
            turn = if (pawnMutationCell != null) it.turn else turn,
            gameCondition = if (pawnMutationCell != null) GameCondition.Mutation(
                pawnMutationCell,
                it.turn
            ) else getGameCondition(board, turn),
            moveCount = it.moveCount + 1
        )
    }

    private fun pawnMutationCell(board: Board): Cell? {
        return board.cellsFlatten
            .filter { it.figure is Pawn && it.figure.color == gameState.value.turn }
            .find { it.number == CellNumber.N8 || it.number == CellNumber.N1 }
    }
}