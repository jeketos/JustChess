package game.ui.auth.splash

import game.api.ApiClient
import game.api.response.Room
import game.api.response.User
import game.data.SocketEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    val state = MutableSharedFlow<SplashState>()

    fun findGame() {
        scope.launch(Dispatchers.IO) {
            state.emit(SplashState.FindGame)
            runCatching {
                val user = ApiClient.signUp()
                val room = ApiClient.findGame(user.uid)
                val roomId = room.uid
                ApiClient.webSocketEvents(roomUid = roomId, userUid = user.uid).collect {
                    println("##### event collected - $it")
                    when (it) {
                        SocketEvents.Start -> {
                            state.emit(SplashState.Success(room = ApiClient.getRoom(roomId), user = user))
                        }
                        else -> {}
                    }
                }
            }.onSuccess {

            }.onFailure {
                it.printStackTrace()
                println("failure")
            }

        }
    }
}

sealed class SplashState {
    object FindGame: SplashState()
    class Success(val room: Room, val user: User): SplashState()
}