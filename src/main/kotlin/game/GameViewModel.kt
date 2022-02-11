package game

import game.data.*
import game.data.figure.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val boardState = MutableStateFlow(Board())

    val turnState = MutableStateFlow(Turn(color = GameColor.White))

    init {
        boardState.update {
            it.copy(cells = it.cells.map { column ->
                column.map { cell ->
                    when (cell.number) {
                        CellNumber.N2 -> cell.copy(figure = Pawn(GameColor.White))
                        CellNumber.N7 -> cell.copy(figure = Pawn(GameColor.Black))
                        CellNumber.N1 -> initFigures(cell, GameColor.White)
                        CellNumber.N8 -> initFigures(cell, GameColor.Black)
                        else -> cell
                    }
                }
            })
        }
    }

    private fun initFigures(cell: Cell, color: GameColor): Cell {
        val figure = when (cell.name) {
            CellName.A, CellName.H -> Rook(color)
            else -> null
//            CellName.B, CellName.G -> Knight(color)
//            CellName.C, CellName.F -> Bishop(color)
//            CellName.D -> Queen(color)
//            CellName.E -> King(color)
        }
        return cell .copy(figure = figure)
    }

    fun onCellClick(clickedCell: Cell) {
        val selected = boardState.value.selectedCell

        when {
            clickedCell.figure != null && clickedCell.figure.color != turnState.value.color && selected == null -> {}
            clickedCell.figure?.color == turnState.value.color -> {
                update(
                    selected = clickedCell,
                    movePossibilities = clickedCell.figure.calculatePossibleMoves(clickedCell, boardState.value)
                )
            }
            selected?.figure != null -> {
                processFigureMoving(selected.figure, selected, clickedCell)
            }
            clickedCell.figure != null -> update(
                selected = clickedCell,
                movePossibilities = clickedCell.figure.calculatePossibleMoves(clickedCell, boardState.value)
            ) // could be removed
        }
    }

    private fun processFigureMoving(figure: Figure, selected: Cell, clickedCell: Cell) {
        val movePossibilities = boardState.value.movePossibilities ?: return
        println(
            "movePossibilities - ${
                movePossibilities.map { it.cellToMove }.joinToString(", ") { "${it.name}${it.number}" }
            }"
        )
        if (movePossibilities.any { it.cellToMove == clickedCell }) {
            update(
                modifiedCells = listOfNotNull(
                    selected.copy(figure = null),
                    clickedCell.copy(figure = figure.copy(figure.moveCount + 1)),
                    boardState.value.movePossibilities?.firstOrNull { it.cellToAttack != null }
                        ?.cellToAttack?.copy(figure = null)
                ),
                selected = null,
                movePossibilities = null

            )
            turnState.update {
                it.copy(color = it.color.toggle())
            }
        }
    }

    private fun update(
        modifiedCells: List<Cell>? = null,
        selected: Cell? = null,
        movePossibilities: List<AttackedCell>? = null
    ) = scope.launch {
        boardState.update {
            it.copy(
                cells = it.cells.map { cells ->
                    cells.map { cell ->
                        modifiedCells?.find { modified -> cell.id == modified.id } ?: cell
                    }
                },
                selectedCell = selected,
                movePossibilities = movePossibilities
            )
        }
    }
}