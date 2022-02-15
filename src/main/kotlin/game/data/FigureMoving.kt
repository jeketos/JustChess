package game.data

sealed class FigureMoving(val cellToMove: Cell) {
    class Default(cellToMove: Cell): FigureMoving(cellToMove)
    class Pawn(cellToMove: Cell, val attackedCell: Cell?): FigureMoving(cellToMove)
    class King(cellToMove: Cell, val rookCell: Cell?, val castlingCell: Cell?): FigureMoving(cellToMove)
}
