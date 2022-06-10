package game.data.figure

import game.data.*
import game.util.toInt
import kotlin.experimental.and
import kotlin.experimental.or

abstract class Figure(
    val moveCount: Int,
    val color: GameColor,
    val image: String
) {

    object CompressedValue {
        val Mask: Short = 0b0000_0_111_000_000_0
        val Null: Short = 0b0000_0_000_000_000_0
        val Pawn: Short = 0b0000_0_001_000_000_0
        val Bishop: Short = 0b0000_0_010_000_000_0
        val Knight: Short = 0b0000_0_011_000_000_0
        val Rook: Short = 0b0000_0_100_000_000_0
        val Queen: Short = 0b0000_0_101_000_000_0
        val King: Short = 0b0000_0_110_000_000_0
    }

    abstract val name: String

    abstract val movePossibilities: List<List<Point>>

    abstract fun copy(
        moveCount: Int = this.moveCount
    ): Figure

    open fun calculatePossibleMoves(currentCell: Cell, board: Board): List<FigureMoving> {
        val moves = movePossibilities.flatMap { listOfPoints ->
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

        return moves
    }

    override fun toString(): String =
        "$name $color $moveCount"

}

fun Figure?.compressedValue(): Short =
    when (this) {
        is Pawn -> Figure.CompressedValue.Pawn
        is Bishop -> Figure.CompressedValue.Bishop
        is Knight -> Figure.CompressedValue.Knight
        is Rook -> Figure.CompressedValue.Rook
        is Queen -> Figure.CompressedValue.Queen
        is King -> Figure.CompressedValue.King
        else -> Figure.CompressedValue.Null
    } or
            this?.color.compressedValueForFigure() or
            ((this?.moveCount ?: 0) > 0).toInt().shl(11).toShort()

fun Short.decompressFigure(): Figure? {
    val color = this.decompressFigureColor()
    val moves = this.toInt().shr(11).and(1)
    return when (this.and(Figure.CompressedValue.Mask)) {
        Figure.CompressedValue.Pawn -> Pawn(color, moves)
        Figure.CompressedValue.Bishop -> Bishop(color, moves)
        Figure.CompressedValue.Knight -> Knight(color, moves)
        Figure.CompressedValue.Rook -> Rook(color, moves)
        Figure.CompressedValue.Queen -> Queen(color, moves)
        Figure.CompressedValue.King -> King(color, moves)
        else -> null
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
    override val name: String
        get() = "Pawn"

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
                        couldAttackPawnOnEmptyCell -> FigureMoving.Pawn(
                            cellToMove = attack,
                            attackedCell = previousCell
                        )
                        else -> null
                    }
                }
            )
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

    override val name: String
        get() = "Rook"

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

    override val name: String
        get() = "Bishop"

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

    override val name: String
        get() = "Knight"

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

    override val name: String
        get() = "Queen"

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

    override val name: String
        get() = "King"

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
            isKingAbleForCastlingToRight -> calculatePossibleMoves + FigureMoving.King(
                cellAfterNext!!,
                rookCell,
                nextCell
            )
            isKingAbleForCastlingToLeft -> calculatePossibleMoves + FigureMoving.King(
                cellLeftAfterNext!!,
                rookLeftCell,
                nextLeftCell
            )
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
