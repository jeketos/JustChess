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
                    when (cell.position) {
                        2 -> cell.copy(figure = Pawn(GameColor.White))
                        7 -> cell.copy(figure = Pawn(GameColor.Black))
                        1 -> initFigures(cell, GameColor.White)
                        8 -> initFigures(cell, GameColor.Black)
                        else -> cell
                    }
                }
            })
        }
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

    fun onCellClick(clickedCell: Cell) {
        val selected = boardState.value.cells.flatten().find { it.selected }
        when {
            clickedCell.figure != null && clickedCell.figure.color != turnState.value.color && selected == null -> {}
            clickedCell.figure?.color == turnState.value.color -> {
                update(selected?.copy(selected = false))
                update(clickedCell.copy(selected = true))
            }
            selected?.figure != null -> {
                processFigureMoving(selected.figure, selected, clickedCell)
            }
            clickedCell.figure != null -> update(clickedCell.copy(selected = true))
        }
    }

    private fun processFigureMoving(figure: Figure, selected: Cell, clickedCell: Cell) {
        val canMove = figure.canMove(selected, clickedCell, boardState.value)
        println("canMove - $canMove")
        if (canMove) {
            update(selected.copy(selected = false, figure = null))
            update(clickedCell.copy(figure = figure.copy(figure.moveCount + 1)))
            turnState.update {
                it.copy(color = it.color.toggle())
            }
        }
    }

    private fun update(new: Cell?) = scope.launch {
        boardState.update {
            it.copy(cells = it.cells.map { cells ->
                cells.map { cell ->
                if (cell.position == new?.position && cell.name == new.name) new
                else cell
                }
            })
        }
    }
}