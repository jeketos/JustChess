package game.ui.online

import game.api.ApiClient
import game.api.request.MoveRequest
import game.api.response.Room
import game.api.response.User
import game.controller.GameController
import game.controller.GameState
import game.data.*
import game.data.figure.Figure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OnlineGameViewModel(
    private val room: Room,
    private val user: User
) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private val gameController = GameController()
    val userColor = if (room.players.eagle?.uid == user.uid) GameColor.White else GameColor.Black
    val gameState: StateFlow<GameState> = gameController.gameState
    val closeEvent = MutableSharedFlow<Unit>()

    init {
        scope.launch(Dispatchers.IO) {
            gameState.collect {
                if (it.gameCondition == GameCondition.Mate) {
                    ApiClient.closeWebSocket()
                }
            }
        }
        gameController.setStateByRoom(room, user, userColor)
        scope.launch(Dispatchers.IO) {
            ApiClient.webSocketEvents(
                roomUid = room.uid
            ).collect {
                when (it) {
                    SocketEvents.Update -> {
                        runCatching {
                            val remoteRoom = ApiClient.getRoom(room.uid)
                            gameController.setStateByRoom(remoteRoom, user, userColor)
                        }.onFailure {
                            val achieved = ApiClient.getAchievedRoom(room.uid)
                            gameController.setStateByRoom(achieved, user, userColor)
                        }
                    }
                    else -> {}
                }
            }
        }
        scope.launch(Dispatchers.IO) {
            gameController.gameState.map {
                it.turn
            }.distinctUntilChanged()
                .collect { color ->
                    if (color != userColor && gameState.value.moveCount > 0) {
                        ApiClient.makeMove(
                            MoveRequest(
                                roomUid = room.uid,
                                board = gameState.value.board.compressToString()
                            )
                        )
                    }
                }
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
            ApiClient.giveUp()
        }
    }

    fun onCloseClick() {
        scope.launch {
            runCatching {
                if (gameState.value.gameCondition != GameCondition.Mate) {
                    ApiClient.giveUp()
                }
                ApiClient.closeWebSocket()
            }
            closeEvent.emit(Unit)
        }
    }
}