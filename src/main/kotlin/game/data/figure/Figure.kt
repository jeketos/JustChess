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

    open fun calculatePossibleMoves(currentCell: Cell, board: Board): List<FigureMoving> =
        movePossibilities.flatMap { listOfPoints ->
        val points = listOfPoints.mapNotNull { point ->
            val name = (currentCell.name + point.x) ?: return@mapNotNull null
            val number = (currentCell.number + point.y) ?: return@mapNotNull null
            board.cellsFlatten.find { it.name == name && it.number == number }
        }
        val figureIndex = points.indexOfFirst { it.figure != null }
        val take = when {
            figureIndex == -1 -> points.size
            points[figureIndex].figure?.color == color -> figureIndex
            else -> figureIndex + 1
        }
        points.take(take)
    }.map {
        FigureMoving.Default(cellToMove = it)
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

    override val movePossibilities: List<List<Point>> = listOf(
        if (moveCount == 0) {
            listOf(Point(x = 0, y = 1.colorMoveDirection()), Point(x = 0, y = 2.colorMoveDirection()))
        } else {
            listOf(Point(0, 1.colorMoveDirection()))
        }
    )

    override fun calculatePossibleMoves(currentCell: Cell, board: Board): List<FigureMoving> {
        return movePossibilities.flatMap { list ->
            list.mapNotNull { point ->
                val name = (currentCell.name + point.x) ?: return@mapNotNull null
                val number = (currentCell.number + point.y) ?: return@mapNotNull null
                board.cellsFlatten.find { it.name == name && it.number == number }
            }.filter { cell ->
                cell.figure == null
            }.map {
                println("map movePossibilities- ${it.id}")
                FigureMoving.Default(cellToMove = it)
            }.plus(
                attackPossibilities.mapNotNull { point ->
                    val name = (currentCell.name + point.x) ?: return@mapNotNull null
                    val number = (currentCell.number + point.y) ?: return@mapNotNull null
                    board.cellsFlatten.find { it.name == name && it.number == number }
                }.mapNotNull { attack ->
                    val previousCell =
                        board.cellsFlatten.find { it.name == attack.name && it.number == attack.number - 1.colorMoveDirection() }
                    val couldAttackPawnOnEmptyCell = previousCell?.figure is Pawn &&
                            previousCell.figure.moveCount == 1 &&
                            attack.name == previousCell.name &&
                            (previousCell.number == CellNumber.N4 || previousCell.number == CellNumber.N5) &&
                            previousCell.figure.color != color
                    when {
                        attack.figure != null && attack.figure.color != color -> FigureMoving.Default(cellToMove = attack)
                        couldAttackPawnOnEmptyCell -> FigureMoving.Pawn(cellToMove = attack, attackedCell = previousCell)
                        else -> null
                    }
                }.also {
                    println("map attackPossibilities- ${it.joinToString(", ") { it.cellToMove.id }}")
                }
            )
        }.also { attacked ->
            println("currentCell - ${currentCell.id}, movePossibilities - ${
                attacked.map { it.cellToMove }.joinToString(", ") { "${it.name}${it.number}" }
            }")
        }
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

    override val movePossibilities: List<List<Point>> =
        listOf(
            List(7) {
                Point(it + 1, 0)
            },
            List(7) {
                Point(0, it + 1)
            },
            List(7) {
                Point((it + 1).unaryMinus(), 0)
            },
            List(7) {
                Point(0, (it + 1).unaryMinus())
            }
        )

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

    override val movePossibilities: List<List<Point>> = listOf(
        List(7) {
            Point(it + 1, it + 1)
        },
        List(7) {
            Point((it + 1).unaryMinus(), it + 1)
        },
        List(7) {
            Point(it + 1, (it + 1).unaryMinus())
        },
        List(7) {
            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
        }
    )

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

    override val movePossibilities: List<List<Point>> =
        listOf(
            listOf(Point(1, 2)),
            listOf(Point(2, 1)),
            listOf(Point(2, -1)),
            listOf(Point(1, -2)),
            listOf(Point(-1, -2)),
            listOf(Point(-2, -1)),
            listOf(Point(-2, 1)),
            listOf(Point(-1, 2)),
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

    override val movePossibilities: List<List<Point>> = listOf(
        List(7) {
            Point(it + 1, 0)
        }, List(7) {
            Point(0, it + 1)
        }, List(7) {
            Point((it + 1).unaryMinus(), 0)
        }, List(7) {
            Point(0, (it + 1).unaryMinus())
        }, List(7) {
            Point(it + 1, it + 1)
        }, List(7) {
            Point((it + 1).unaryMinus(), it + 1)
        }, List(7) {
            Point(it + 1, (it + 1).unaryMinus())
        }, List(7) {
            Point((it + 1).unaryMinus(), (it + 1).unaryMinus())
        }
    )

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

    override val movePossibilities: List<List<Point>> = listOf(
            listOf(Point(0, 1)),
            listOf(Point(0, -1)),
            listOf(Point(1, 0)),
            listOf(Point(1, 1)),
            listOf(Point(1, -1)),
            listOf(Point(-1, 0)),
            listOf(Point(-1, 1)),
            listOf(Point(-1, -1)),
        )

    override fun copy(moveCount: Int): King {
        return King(color, moveCount)
    }

    override fun calculatePossibleMoves(currentCell: Cell, board: Board): List<FigureMoving> {
        val calculatePossibleMoves = super.calculatePossibleMoves(currentCell, board)

        val rookCell = next(currentCell, Point(3, 0), board)
        val nextCell = next(currentCell, Point(1, 0), board)
        val cellAfterNext = next(currentCell, Point(2, 0), board)
        val isKingAbleForCastlingToRight = moveCount == 0 && nextCell!!.figure == null &&
                cellAfterNext!!.figure == null &&
                rookCell!!.figure is Rook && rookCell.figure!!.moveCount == 0

        val rookLeftCell = next(currentCell, Point(-4, 0), board)
        val nextLeftCell = next(currentCell, Point(-1, 0), board)
        val cellLeftAfterNext = next(currentCell, Point(-2, 0), board)
        val cellLeftDoubleAfterNext = next(currentCell, Point(-3, 0), board)

        val isKingAbleForCastlingToLeft = moveCount == 0 && nextLeftCell!!.figure == null &&
                cellLeftAfterNext!!.figure == null &&
                cellLeftDoubleAfterNext!!.figure == null &&
                rookLeftCell!!.figure is Rook && rookLeftCell.figure!!.moveCount == 0

        return when {
            isKingAbleForCastlingToRight -> calculatePossibleMoves + FigureMoving.King(cellAfterNext!!, rookCell, nextCell)
            isKingAbleForCastlingToLeft -> calculatePossibleMoves + FigureMoving.King(cellLeftAfterNext!!, rookLeftCell, nextLeftCell)
            else -> calculatePossibleMoves
        }
    }

    private fun next(currentCell: Cell, point: Point, board: Board): Cell? {
        val name = (currentCell.name + point.x) ?: return null
        val number = (currentCell.number + point.y) ?: return null
        return board.cellsFlatten.find { it.name == name && it.number == number }
    }

}


fun GameColor.image(white: String, black: String) = when (this) {
    GameColor.White -> white
    GameColor.Black -> black
}
