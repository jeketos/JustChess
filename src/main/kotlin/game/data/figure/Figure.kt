package game.data.figure

import game.data.Cell
import game.data.GameColor
import game.data.Point

abstract class Figure(
    val moveCount: Int,
    val color: GameColor,
    val image: String
) {
    abstract fun movePossibilities(): List<Point>

    abstract fun copy(
        moveCount: Int = this.moveCount
    ): Figure

    open fun canMove(from: Cell, to: Cell): Boolean {
        if (to.figure?.color == color) return false

        return movePossibilities().any {
            (from.name + it.y) == to.name && from.position + it.x == to.position
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

    override fun movePossibilities(): List<Point> =
        if (moveCount == 0) {
            when (color) {
                GameColor.Black -> listOf(Point(-1,0), Point(-2, 0))
                GameColor.White -> listOf(Point(1,0), Point(2, 0))
            }
        } else {
            when (color) {
                GameColor.Black -> listOf(Point(-1,0))
                GameColor.White -> listOf(Point(1,0))
            }
        }

    override fun copy(moveCount: Int): Figure {
        return Pawn(color, moveCount)
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

    override fun movePossibilities(): List<Point> =
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

    override fun movePossibilities(): List<Point> =
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

    override fun movePossibilities(): List<Point> =
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

    override fun movePossibilities(): List<Point> =
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

    override fun movePossibilities(): List<Point> =
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

    override fun canMove(from: Cell, to: Cell): Boolean {
        return super.canMove(from, to)
    }

    override fun copy(moveCount: Int): King {
        return King(color, moveCount)
    }

}



fun GameColor.image(white: String, black: String) = when (this) {
    GameColor.White -> white
    GameColor.Black -> black
}
