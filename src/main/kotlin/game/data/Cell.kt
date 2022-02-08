package game.data

import game.data.figure.Figure

data class Cell(
    val name: CellName,
    val number: CellNumber,
    val color: GameColor,
    val figure: Figure? = null
) {
    val id: String = name.toString() + number.toString()
}

data class AttackedCell(
    val cellToMove: Cell,
    val cellToAttack: Cell?
)

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
