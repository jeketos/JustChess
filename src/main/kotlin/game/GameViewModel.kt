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

    fun onCellClick(cell: Cell) {
        println("on cell clicked")
        val selected = boardState.value.cells.flatten().find { it.selected }
        if (selected?.figure != null) {
            val canMove = selected.figure.canMove(selected, cell, boardState.value)
            println("canMove - $canMove")
            if (canMove) {
                update(selected.copy(selected = false, figure = null))
                update(cell.copy(figure = selected.figure.copy(selected.figure.moveCount + 1)))
            }
            //can move logic
        } else if (cell.figure != null) {
            update(cell.copy(selected = true))
        }
    }

    private fun update(new: Cell) = scope.launch {
        boardState.update {
            it.copy(cells = it.cells.map {
                it.map { cell ->
                if (cell.position == new.position && cell.name == new.name) new
                else cell
                }
            })
        }
    }
}