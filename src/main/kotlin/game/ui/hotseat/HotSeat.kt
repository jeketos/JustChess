package game.ui.hotseat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import game.data.*
import game.data.figure.Bishop
import game.data.figure.Knight
import game.data.figure.Queen
import game.data.figure.Rook
import game.navigation.NavController


@Composable
fun HotSeat(
    navController: NavController,
    viewModel: HotSeatGameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()

    val board = gameState.board

    MaterialTheme {
        Row {
            Column {
                board.cells.forEach {
                    Row {
                        it.forEach { cell ->
                            CellContent(
                                cell = cell,
                                selected = cell.id == gameState.selectedCell?.id,
                                highlighted = gameState.movePossibilities?.any { it.cellToMove.id == cell.id} ?: false
                            ) { clickedCell ->
                                viewModel.onCellClick(clickedCell)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Column(Modifier.align(Alignment.Center)) {
                    val text = when(val gameCondition = gameState.gameCondition) {
                        is GameCondition.Mutation -> "${gameCondition.color}'s Pawn Mutation"
                        GameCondition.Check -> "${gameState.turn.name} turn, CHECK!"
                        GameCondition.Mate -> "GAME OVER, ${gameState.turn.name}`s CHECK & MATE!"
                        GameCondition.Stalemate ->  "GAME OVER, STALEMATE!"
                        GameCondition.NothingSpecial -> "${gameState.turn.name} turn"
                    }
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 16.dp),
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        text = text
                    )
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 16.dp),
                        onClick = {
                            viewModel.startNewGame()
                        }
                    ) {
                        Text(text = "New game")
                    }
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd).size(64.dp),
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(modifier = Modifier.padding(16.dp), painter = painterResource("ic_close.png"), contentDescription = null)
                }
            }
        }


        val mutation = gameState.gameCondition as? GameCondition.Mutation
        if (mutation != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(color = Color(0x44000000)).clickable {  }
            ) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    val color = mutation.color
                    listOf(
                        Cell(CellName.A, CellNumber.N1, color, Knight(color)),
                        Cell(CellName.A, CellNumber.N1, color, Bishop(color)),
                        Cell(CellName.A, CellNumber.N1, color, Rook(color)),
                        Cell(CellName.A, CellNumber.N1, color, Queen(color)),
                    ).forEach {
                        CellContent(
                            cell = it,
                            selected = false,
                            highlighted = false
                        ) { cell ->
                            viewModel.onMutationSelected(mutation.cell, cell.figure!!)
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