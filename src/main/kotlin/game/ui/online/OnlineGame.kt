package game.ui.online

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import game.ui.hotseat.CellContent

@Composable
fun OnlineGame(
    navController: NavController,
    viewModel: OnlineGameViewModel
) {
    val gameState by viewModel.gameState.collectAsState()

    val board = gameState.board

    LaunchedEffect(Unit) {
        viewModel.closeEvent.collect {
            navController.popBackStack()
        }
    }
    MaterialTheme {
        Row {
            Column {
                val cells = if (viewModel.userColor == GameColor.White) board.cells else board.cells.reversed()
                cells.forEach {
                    Row {
                        it.forEach { cell ->
                            CellContent(
                                cell = cell,
                                selected = cell.id == gameState.selectedCell?.id,
                                highlighted = gameState.movePossibilities?.any { it.cellToMove.id == cell.id } ?: false
                            ) { clickedCell ->
                                viewModel.onCellClick(clickedCell)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Column(Modifier.align(Alignment.Center)) {

                    val pronoun = getPronoun(gameState.turn, viewModel.userColor)
                    val text = when (gameState.gameCondition) {
                        is GameCondition.Mutation -> "$pronoun Pawn Mutation"
                        GameCondition.Check -> "$pronoun turn, CHECK!"
                        GameCondition.Mate -> "GAME OVER, $pronoun CHECK & MATE!"
                        GameCondition.Stalemate -> "GAME OVER, STALEMATE!"
                        GameCondition.NothingSpecial -> "$pronoun turn"
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
                            viewModel.giveUp()
                        }
                    ) {
                        Text(text = "Give up")
                    }
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd).size(64.dp),
                    onClick = {
                        viewModel.onCloseClick()
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(16.dp),
                        painter = painterResource("ic_close.png"),
                        contentDescription = null
                    )
                }
            }
        }


        val mutation = gameState.gameCondition as? GameCondition.Mutation
        if (mutation != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(color = Color(0x44000000)).clickable { }
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

fun getPronoun(turn: GameColor, ownColor: GameColor): String {
    val pronoun = if (turn == ownColor) "YOURS" else "ENEMY'S"
    return "It is $pronoun (${turn})"
}
