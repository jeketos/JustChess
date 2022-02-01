package game.data.figure

import game.data.Board
import game.data.Cell
import game.data.GameColor
import game.data.Point

abstract class Figure(
    val moveCount: Int,
    val color: GameColor,
    val image: String
) {
    abstract val movePossibilities: List<Point>

    abstract fun copy(
        moveCount: Int = this.moveCount
    ): Figure

    open fun canMove(from: Cell, to: Cell, board: Board): Boolean {
        if (to.figure?.color == color) return false

        return movePossibilities.any {
            (from.name + it.x) == to.name && from.position + it.y == to.position
        }
    }
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

    override val movePossibilities: List<Point> =
        if (moveCount == 0) {
            listOf(Point(x = 0, y = 1.colorMoveDirection()), Point(x = 0, y = 2.colorMoveDirection()))
        } else {
            listOf(Point(0, 1.colorMoveDirection()))
        }

    override fun canMove(from: Cell, to: Cell, board: Board): Boolean {
        println("to: ${to.name} ${to.position}")
        val figureColor = to.figure?.color
        if (figureColor == color) return false

        val previousCell = board.cellsFlatten.find { it.name == to.name && it.position == to.position - 1.colorMoveDirection() }
        println("pawnCell - $previousCell")
        return when {
            previousCell?.figure is Pawn && previousCell.figure.moveCount == 1 && canAttack(from, to) -> true // TODO: modify board somehow
            figureColor != null -> canAttack(from, to)

            else -> {
                movePossibilities.any {
                    (from.name + it.x) == to.name && from.position + it.y == to.position
                } && (previousCell?.figure == null || previousCell.figure == this)
            }

        }

    }

    private fun canAttack(from: Cell, to: Cell) = attackPossibilities.any {
        val moveToName = from.name + it.x
        val moveToPosition = from.position + it.y
        println("$moveToName $moveToPosition")
        moveToName == to.name && moveToPosition == to.position
    }

    override fun copy(moveCount: Int): Figure {
        return Pawn(color, moveCount)
    }

    private fun Int.colorMoveDirection(): Int =
        when (color) {
            GameColor.Black -> this.unaryMinus()
            GameColor.White -> this.unaryPlus()
        }

}

class Rook(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("rookWhite.png", "rookBlack.png")
) {

    override val movePossibilities: List<Point> =
        List(7) {
            Point(it + 1, 0)
        } + List(7) {
            Point(0, it + 1)
        } + List(7) {
            Point((it + 1).unaryMinus(), 0)
        } + List(7) {
            Point(0, (it + 1).unaryMinus())
        }

    override fun copy(moveCount: Int): Rook {
        return Rook(color, moveCount)
    }

}

class Bishop(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("bishopWhite.png", "bishopBlack.png")
) {

    override val movePossibilities: List<Point> =
        List(7) {
            Point(it + 1, it + 1)
        } + List(7) {
            Point((it + 1).unaryMinus(), it + 1)
        } + List(7) {
            Point(it + 1, (it + 1).unaryMinus())
        } + List(7) {
            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
        }

    override fun copy(moveCount: Int): Bishop {
        return Bishop(color, moveCount)
    }

}

class Knight(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("knightWhite.png", "knightBlack.png")
) {

    override val movePossibilities: List<Point> =
        listOf(
            Point(1, 2),
            Point(2, 1),
            Point(2, -1),
            Point(1, -2),
            Point(-1, -2),
            Point(-2, -1),
            Point(-2, 1),
            Point(-1, 2),
        )

    override fun copy(moveCount: Int): Knight {
        return Knight(color, moveCount)
    }

}

class Queen(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("queenWhite.png", "queenBlack.png")
) {

    override val movePossibilities: List<Point> =
        List(7) {
            Point(it + 1, 0)
        } + List(7) {
            Point(0, it + 1)
        } + List(7) {
            Point((it + 1).unaryMinus(), 0)
        } + List(7) {
            Point(0, (it + 1).unaryMinus())
        } + List(7) {
            Point(it + 1, it + 1)
        } + List(7) {
            Point((it + 1).unaryMinus(), it + 1)
        } + List(7) {
            Point(it + 1, (it + 1).unaryMinus())
        } + List(7) {
            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
        }

    override fun copy(moveCount: Int): Queen {
        return Queen(color, moveCount)
    }

}


class King(
    color: GameColor,
    moveCount: Int = 0
) : Figure(
    moveCount = moveCount,
    color = color,
    image = color.image("kingWhite.png", "kingBlack.png")
) {

    override val movePossibilities: List<Point> =
        listOf(
            Point(0, 1),
            Point(0, -1),
            Point(1, 0),
            Point(1, 1),
            Point(1, -1),
            Point(-1, 0),
            Point(-1, 1),
            Point(-1, -1),
        )

    override fun copy(moveCount: Int): King {
        return King(color, moveCount)
    }

}


fun GameColor.image(white: String, black: String) = when (this) {
    GameColor.White -> white
    GameColor.Black -> black
}
