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