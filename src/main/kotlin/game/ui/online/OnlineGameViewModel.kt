package game.ui.online

import game.api.ApiClient
import game.api.request.MoveRequest
import game.api.response.Room
import game.api.response.User
import game.controller.GameController
import game.controller.GameState
import game.data.Cell
import game.data.GameColor
import game.data.compressToString
import game.data.figure.Figure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OnlineGameViewModel(val room: Room, val user: User) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private val gameController = GameController()
    private val userColor = if (room.players.eagle?.uid == user.uid) GameColor.White else GameColor.Black
    val gameState: StateFlow<GameState> = gameController.gameState

    init {
        gameController.setStateByRoom(room, user, userColor)
        scope.launch(Dispatchers.IO) {
            if (userColor == GameColor.Black) {
                simulateWebSocket()
            }
        }
        scope.launch(Dispatchers.IO) {
            gameController.gameState.map {
                it.turn
            }.distinctUntilChanged()
                .collect { color ->
                    println("#### color - $color, moveCount - ${gameState.value.moveCount}")
                    if (color != userColor && gameState.value.moveCount > 0) {
                        println("#### userUid - ${user.uid}, room.uid - ${room.uid}")
                        val room = ApiClient.makeMove(MoveRequest(userUid = user.uid, roomUid = room.uid, gameState.value.board.compressToString()))
                        gameController.setStateByRoom(room, user, userColor)
                        simulateWebSocket()
                    }
                }
        }
    }

    private suspend fun simulateWebSocket() {
        delay(1000)
        val remoteRoom = ApiClient.getRoom(room.uid)
        if (remoteRoom.gameState.turn == user.uid) {
            gameController.setStateByRoom(remoteRoom, user, userColor)
            return
        } else {
            simulateWebSocket()
        }
    }


    fun onCellClick(clickedCell: Cell) {
        if (gameState.value.turn == userColor) {
            gameController.onCellClick(clickedCell)
        }
    }

    fun onMutationSelected(selected: Cell, figure: Figure) {
        gameController.onMutationSelected(selected, figure)
    }

    fun giveUp() {
        scope.launch {
            val room = ApiClient.giveUp(user.uid)
            //todo
        }
    }
}