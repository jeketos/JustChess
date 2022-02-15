package game.data

data class Board(
    val size: BoardSize = BoardSize(8, 8),
    val cells: List<List<Cell>> = size.let {
        List(it.width) { width ->
            List(it.height) { height ->
                val name = CellName.values()[height]
                Cell(
                    name = name,
                    number = CellNumber.values()[it.width - width - 1],
                    color = name.color(CellNumber.values()[it.width - width - 1])
                )
            }
        }
    },
    val selectedCell: Cell? = null,
    val movePossibilities: List<FigureMoving>? = null
) {
    val cellsFlatten = cells.flatten()
}

fun Board.getFromCellByPoint(cell: Cell, point: Point): Cell? {
    val x = (cell.name + point.x)?.x
    val y = (cell.number + point.y)?.y
    return runCatching {
        cells[x!!][y!!]
    }.getOrNull()
}

class BoardSize(
    val width: Int,
    val height: Int
)

fun main() {
    Board().apply {
        println(cells.joinToString("\n") {
            it.joinToString(" ") {
                "${it.name.id}${it.number.alias}"
            }
        })
        val cell = cells[0][0]
        println("${cell.name.id}${cell.number.alias}")
        getFromCellByPoint(cell, Point(1, 1))?.also {
            println("${it.name.id}${it.number.alias}")
        }
    }
}