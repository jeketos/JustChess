package game.data

import game.data.figure.Figure

data class Cell(
    val name: CellName,
    val position: Int,
    val color: GameColor,
    val figure: Figure? = null,
    val selected: Boolean = false
) {
//    override fun equals(other: Any?): Boolean {
//        return other is Cell && other.name == name && other.position == position
//    }
//
//    override fun hashCode(): Int {
//        var result = name.hashCode()
//        result = 31 * result + position
//        result = 31 * result + color.hashCode()
//        result = 31 * result + (figure?.hashCode() ?: 0)
//        result = 31 * result + selected.hashCode()
//        return result
//    }
}

enum class CellName(val id: String, val y: Int) {
    A("A", 0),
    B("B", 1),
    C("C", 2),
    D("D", 3),
    E("E", 4),
    F("F", 5),
    G("G", 6),
    H("H", 7);

    operator fun plus(y: Int): CellName? = values().find { this.y + y == it.y }
}

fun CellName.color(position: Int): GameColor {
   val index = CellName.values().indexOf(this)
    val initialColor = if (index % 2 == 0) {
        GameColor.Black
    } else GameColor.White

    return if (position % 2 == 0) {
        initialColor
    } else initialColor.toggle()
}
