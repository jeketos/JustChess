package game.data

import game.data.figure.Figure
import game.data.figure.compressedValue
import game.data.figure.decompressFigure
import kotlin.experimental.or

data class Cell(
    val name: CellName,
    val number: CellNumber,
    val color: GameColor,
    val figure: Figure? = null
) {
    val id: String = name.toString() + number.toString()

    override fun toString(): String {
        return id
    }
}

/**
 * Cell representation in Short
 *
 *  0b000|0|0|000|000|000|0|
 *       |0|1| 2 | 3 | 4 |5|
 *        -----------------
 *  0. Have moves - 0 - no
 *                  1 - yes
 *
 *  1.FigureColor - 0 - black
 *                  1 - white
 *
 *  2. Figure -     000 - null
 *                  001 - Pawn
 *                  010 - Bishop
 *                  011 - Knight
 *                  100 - Rook
 *                  101 - Queen
 *                  110 - King
 *
 *  3.CellNumber -  000 - N1
 *                  001 - N2
 *                  010 - N3
 *                  011 - N4
 *                  100 - N5
 *                  101 - N6
 *                  110 - N7
 *                  111 - N8
 *
 *  4. CellName -   000 - A
 *                  001 - B
 *                  010 - C
 *                  011 - D
 *                  100 - E
 *                  101 - F
 *                  110 - G
 *                  111 - H
 *
 *  5.CellColor -   0 - black
 *                  1 - white
 */
fun Cell.compress(): Short =
    color.compressedValueForCell() or
            name.compressedValue() or
            number.compressedValue() or
            figure.compressedValue()

fun Short.decompressCell(): Cell {
    val color = decompressCellColor()
    val figure = decompressFigure()
    val name = decompressCellName()
    val number = decompressCellNumber()
    return Cell(
        color = color,
        name = name,
        number = number,
        figure = figure
    )
}

fun Cell.isUnderAttack(defendingColor: GameColor, board: Board): Boolean =
    board.cellsFlatten.filter { it.figure != null && it.figure.color != defendingColor }
        .flatMap { cell ->
            cell.figure!!.calculatePossibleMoves(cell, board)
        }
        .firstOrNull {
            it.cellToMove.id == this.id
        } != null


enum class CellName(val id: String, val x: Int) {
    A("A", 0),
    B("B", 1),
    C("C", 2),
    D("D", 3),
    E("E", 4),
    F("F", 5),
    G("G", 6),
    H("H", 7);

    operator fun plus(x: Int): CellName? = values().find { this.x + x == it.x }
}

fun CellName.compressedValue(): Short = this.x.shl(4).toShort()

fun Short.decompressCellName(): CellName {
    val x = this.toInt().shr(4).and(0b111)
    return CellName.values().find { it.x == x } ?: CellName.A
}

fun CellName.color(cellNumber: CellNumber): GameColor {
    val index = CellName.values().indexOf(this)
    val initialColor = if (index % 2 == 0) {
        GameColor.Black
    } else GameColor.White

    return if (cellNumber.y % 2 == 0) {
        initialColor
    } else initialColor.toggle()
}

enum class CellNumber(val alias: String, val y: Int) {
    N1("1", 0),
    N2("2", 1),
    N3("3", 2),
    N4("4", 3),
    N5("5", 4),
    N6("6", 5),
    N7("7", 6),
    N8("8", 7);

    operator fun plus(y: Int): CellNumber? = values().find { this.y + y == it.y }

    operator fun minus(y: Int): CellNumber? = values().find { this.y - y == it.y }

    override fun toString(): String {
        return alias
    }
}

fun CellNumber.compressedValue(): Short = this.y.shl(1).toShort()

fun Short.decompressCellNumber(): CellNumber {
    val y = this.toInt().shr(1).and(0b111)
    return CellNumber.values().find { it.y == y } ?: CellNumber.N1
}