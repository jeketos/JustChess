package game.data

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

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
    }
) {
    val cellsFlatten = cells.flatten()

    override fun toString(): String {
        return cellsFlatten.chunked(8)
            .joinToString("\n") {
                it.joinToString("\t|\t") { cell ->
                    "$cell ${cell.color} ${cell.figure}${if (cell.figure == null) "\t\t" else ""}"
                }
            }
    }
}

fun Board.getFromCellByPoint(cell: Cell, point: Point): Cell? {
    val x = (cell.name + point.x)?.x
    val y = (cell.number + point.y)?.y
    return runCatching {
        cells[x!!][y!!]
    }.getOrNull()
}

fun Board.update(modifiedCells: List<Cell>) = this.copy(
    cells = cells.map { cells ->
        cells.map { cell ->
            modifiedCells.find { modified -> cell.id == modified.id } ?: cell
        }
    }
)

fun Board.compressToString(): String {
    val buffer = ByteBuffer.allocate(128)
    cellsFlatten.forEach {
        val compress = it.compress()
        buffer.putShort(compress)
    }
    val array = buffer.array()
    return Base64.getUrlEncoder().encodeToString(array)
}

fun String.decompressBoard(): Board {
    val array = Base64.getUrlDecoder().decode(this)
    val rawData = ShortArray(array.size.div(2))
    ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(rawData)
    val cells = rawData.map { it.decompressCell() }.chunked(8)
    return Board(cells = cells)
}

class BoardSize(
    val width: Int,
    val height: Int
)