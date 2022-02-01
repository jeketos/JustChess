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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import game.GameViewModel
import game.data.Cell
import game.data.render
import kotlinx.coroutines.flow.collect

@Composable
@Preview
fun App(viewModel: GameViewModel) {


    val board by viewModel.boardState.collectAsState()

    LaunchedEffect("new") {
        viewModel.boardState.collect {
            println("board received - $board")
        }
    }
    println("board redraw - $board")
    MaterialTheme {
        Column {
            board.cells.forEach {
                Row {
                    it.forEach {
                        CellContent(it) { cell ->
                            viewModel.onCellClick(cell)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CellContent(
    cell: Cell,
    onClick: (Cell) -> Unit,
) {
    Box(modifier = Modifier.size(64.dp).background(cell.color.render()).clickable {
            onClick(cell)
        }) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = cell.name.id + cell.position,
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
        if (cell.selected) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Green.copy(alpha = 0.3f)))
        }
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App(GameViewModel())
    }
}
