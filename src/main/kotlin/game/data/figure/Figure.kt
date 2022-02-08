package game.data.figure

import game.data.*

abstract class Figure(
    val moveCount: Int,
    val color: GameColor,
    val image: String
) {
    abstract val movePossibilities: List<List<Point>>

    abstract fun copy(
        moveCount: Int = this.moveCount
    ): Figure

    abstract fun calculatePossibleMoves(currentCell: Cell, board: Board): List<AttackedCell>
}

class Pawn(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("pawnWhite.png", "pawnBlack.png")
) {

    private val attackPossibilities: List<Point> =
        listOf(Point(1, 1.colorMoveDirection()), Point(-1, 1.colorMoveDirection()))

    override val movePossibilities: List<List<Point>> = listOf(
        if (moveCount == 0) {
            listOf(Point(x = 0, y = 1.colorMoveDirection()), Point(x = 0, y = 2.colorMoveDirection()))
        } else {
            listOf(Point(0, 1.colorMoveDirection()))
        }
    )

    override fun calculatePossibleMoves(currentCell: Cell, board: Board): List<AttackedCell> {
        return movePossibilities.flatMap { list ->
            list.mapNotNull { point ->
                val name = (currentCell.name + point.x) ?: return@mapNotNull null
                val number = (currentCell.number + point.y) ?: return@mapNotNull null
                board.cellsFlatten.find { it.name == name && it.number == number }
            }.filter { cell ->
                cell.figure == null
            }.map {
                println("map movePossibilities- ${it.id}")
                AttackedCell(cellToMove = it, cellToAttack = null)
            }.plus(
                attackPossibilities.mapNotNull { point ->
                    val name = (currentCell.name + point.x) ?: return@mapNotNull null
                    val number = (currentCell.number + point.y) ?: return@mapNotNull null
                    board.cellsFlatten.find { it.name == name && it.number == number }
                }.mapNotNull { attack ->
                    val previousCell = board.cellsFlatten.find { it.name == attack.name && it.number == attack.number - 1.colorMoveDirection() }
                    val couldAttackPawnOnEmptyCell = previousCell?.figure is Pawn &&
                            previousCell.figure.moveCount == 1 &&
                            attack.name == previousCell.name &&
                            (previousCell.number == CellNumber.N4 || previousCell.number == CellNumber.N5) &&
                            previousCell.figure.color != color
                    when {
                        attack.figure != null && attack.figure.color != color -> AttackedCell(cellToMove = attack, cellToAttack = null)
                        couldAttackPawnOnEmptyCell -> AttackedCell(cellToMove = attack, cellToAttack = previousCell)
                        else -> null
                    }
                }.also {
                    println("map attackPossibilities- ${it.joinToString(", ") { "${it.cellToMove.id} ${it.cellToAttack?.id}" }}")
                }
            )
        }.also { attacked ->
            println("currentCell - ${currentCell.id}, movePossibilities - ${
                attacked.map { it.cellToMove }.joinToString(", ") { "${it.name}${it.number}" }
            }")
        }
    }
//
//    private fun canAttack(from: Cell, to: Cell) = attackPossibilities.any {
//        val moveToName = from.name + it.x
//        val moveToPosition = from.position + it.y
//        println("$moveToName $moveToPosition")
//        moveToName == to.name && moveToPosition == to.position
//    }
//
//    override fun canMove(from: Cell, to: Cell, board: Board): Boolean {
//        println("to: ${to.name} ${to.position}")
//        val figureColor = to.figure?.color
//        if (figureColor == color) return false
//
//        val previousCell = board.cellsFlatten.find { it.name == to.name && it.position == to.position - 1.colorMoveDirection() }
//        println("pawnCell - $previousCell")
//        return when {
//            previousCell?.figure is Pawn && previousCell.figure.moveCount == 1 && canAttack(from, to) -> true // TODO: modify board somehow
//            figureColor != null -> canAttack(from, to)
//
//            else -> {
//                movePossibilities.any {
//                    (from.name + it.x) == to.name && from.position + it.y == to.position
//                } && (previousCell?.figure == null || previousCell.figure == this)
//            }
//
//        }
//
//    }

    override fun copy(moveCount: Int): Figure {
        return Pawn(color, moveCount)
    }

    private fun Int.colorMoveDirection(): Int =
        when (color) {
            GameColor.Black -> this.unaryMinus()
            GameColor.White -> this.unaryPlus()
        }

}
//
//class Rook(
//    color: GameColor,
//    moveCount: Int = 0
//) : Figure(
//    moveCount = moveCount,
//    color = color,
//    image = color.image("rookWhite.png", "rookBlack.png")
//) {
//
//    override val movePossibilities: List<List<Point>> =
//        listOf(
//            List(7) {
//                Point(it + 1, 0)
//            },
//            List(7) {
//                Point(0, it + 1)
//            },
//            List(7) {
//                Point((it + 1).unaryMinus(), 0)
//            },
//            List(7) {
//                Point(0, (it + 1).unaryMinus())
//            }
//        )
//
//    override fun calculatePossibleMoves(cell: Cell, board: Board): List<Cell> {
//
//    }
//
//    override fun copy(moveCount: Int): Rook {
//        return Rook(color, moveCount)
//    }
//
//}
//
//class Bishop(
//    color: GameColor,
//    moveCount: Int = 0
//) : Figure(
//    moveCount = moveCount,
//    color = color,
//    image = color.image("bishopWhite.png", "bishopBlack.png")
//) {
//
//    override val movePossibilities: List<Point> =
//        List(7) {
//            Point(it + 1, it + 1)
//        } + List(7) {
//            Point((it + 1).unaryMinus(), it + 1)
//        } + List(7) {
//            Point(it + 1, (it + 1).unaryMinus())
//        } + List(7) {
//            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
//        }
//
//    override fun copy(moveCount: Int): Bishop {
//        return Bishop(color, moveCount)
//    }
//
//}
//
//class Knight(
//    color: GameColor,
//    moveCount: Int = 0
//) : Figure(
//    moveCount = moveCount,
//    color = color,
//    image = color.image("knightWhite.png", "knightBlack.png")
//) {
//
//    override val movePossibilities: List<Point> =
//        listOf(
//            Point(1, 2),
//            Point(2, 1),
//            Point(2, -1),
//            Point(1, -2),
//            Point(-1, -2),
//            Point(-2, -1),
//            Point(-2, 1),
//            Point(-1, 2),
//        )
//
//    override fun copy(moveCount: Int): Knight {
//        return Knight(color, moveCount)
//    }
//
//}
//
//class Queen(
//    color: GameColor,
//    moveCount: Int = 0
//) : Figure(
//    moveCount = moveCount,
//    color = color,
//    image = color.image("queenWhite.png", "queenBlack.png")
//) {
//
//    override val movePossibilities: List<Point> =
//        List(7) {
//            Point(it + 1, 0)
//        } + List(7) {
//            Point(0, it + 1)
//        } + List(7) {
//            Point((it + 1).unaryMinus(), 0)
//        } + List(7) {
//            Point(0, (it + 1).unaryMinus())
//        } + List(7) {
//            Point(it + 1, it + 1)
//        } + List(7) {
//            Point((it + 1).unaryMinus(), it + 1)
//        } + List(7) {
//            Point(it + 1, (it + 1).unaryMinus())
//        } + List(7) {
//            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
//        }
//
//    override fun copy(moveCount: Int): Queen {
//        return Queen(color, moveCount)
//    }
//
//}
//
//
//class King(
//    color: GameColor,
//    moveCount: Int = 0
//) : Figure(
//    moveCount = moveCount,
//    color = color,
//    image = color.image("kingWhite.png", "kingBlack.png")
//) {
//
//    override val movePossibilities: List<Point> =
//        listOf(
//            Point(0, 1),
//            Point(0, -1),
//            Point(1, 0),
//            Point(1, 1),
//            Point(1, -1),
//            Point(-1, 0),
//            Point(-1, 1),
//            Point(-1, -1),
//        )
//
//    override fun copy(moveCount: Int): King {
//        return King(color, moveCount)
//    }
//
//}
//
//
fun GameColor.image(white: String, black: String) = when (this) {
    GameColor.White -> white
    GameColor.Black -> black
}
