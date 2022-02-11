// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import game.GameViewModel
import game.data.Cell
import game.data.render

@Composable
@Preview
fun App(viewModel: GameViewModel) {

    val board by viewModel.boardState.collectAsState()
    val turn by viewModel.turnState.collectAsState()

    MaterialTheme {
        Row {
            Column {
                board.cells.forEach {
                    Row {
                        it.forEach { cell ->
                            CellContent(
                                cell = cell,
                                selected = cell.id == board.selectedCell?.id,
                                highlighted = board.movePossibilities?.any { it.cellToMove.id == cell.id} ?: false
                            ) { clickedCell ->
                                viewModel.onCellClick(clickedCell)
                            }
                        }
                    }
                }
            }

            Text(
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 16.dp),
                fontSize = 26.sp,
                text = "${turn.color.name} turn"
            )
        }

    }
}

@Composable
fun CellContent(
    cell: Cell,
    selected: Boolean,
    highlighted: Boolean,
    onClick: (Cell) -> Unit,
) {
    Box(modifier = Modifier.size(64.dp).background(cell.color.render()).clickable {
            onClick(cell)
        }) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = cell.name.id + cell.number,
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        cell.figure?.let {
            Image(
                modifier = Modifier.align(Alignment.Center).size(48.dp),
                painter = painterResource(it.image),
                contentDescription = it.toString()
            )
        }
        if (selected) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Green.copy(alpha = 0.3f)))
        }
        if (highlighted) {
            val highlightColor = if (cell.figure != null) Color.Red.copy(alpha = 0.3f) else Color.Blue.copy(alpha = 0.3f)
            Box(modifier = Modifier.fillMaxSize().background(highlightColor))
        }
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App(GameViewModel())
    }
}
