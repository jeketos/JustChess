package game.data

data class Board(
    val size: BoardSize = BoardSize(8, 8),
    val cells: List<List<Cell>> = size.let {
        List(it.width) { width ->
            List(it.height) { height ->
                val name = CellName.values()[height]
                Cell(
                    name = name,
                    position = it.width - width,
                    color = name.color(it.width - width - 1)
                )
            }
        }
    }
) {
    val cellsFlatten = cells.flatten()
}

class BoardSize(
    val width: Int,
    val height: Int
)