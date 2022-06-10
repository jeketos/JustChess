package game.data

import androidx.compose.ui.graphics.Color

enum class GameColor {
    White,
    Black;

    fun  toggle(): GameColor = if (this == White) Black else White
}

fun GameColor.render(): Color =
    if (this == GameColor.Black) Color(0xFF7DA0B4)
    else Color(0xFFD4DFE3)

fun GameColor.compressedValueForCell(): Short = if (this == GameColor.Black) 0 else 1

fun Short.decompressCellColor(): GameColor = if (this.toInt() and 1 == 1) GameColor.White else GameColor.Black

fun GameColor?.compressedValueForFigure(): Short = (if (this == GameColor.White) 1 else 0).shl(10).toShort()

fun Short.decompressFigureColor(): GameColor = if (this.toInt().shr(10).and(1) == 1) GameColor.White else GameColor.Black