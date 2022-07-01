package game.ui.auth.signUp

import game.api.ApiClient
import game.api.request.SignUpData
import game.api.response.Room
import game.api.response.User
import game.data.CasualState
import game.data.SocketEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SignUpViewModel {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    val state = MutableSharedFlow<CasualState<Pair<Room, User>>>()
    val errorEvent = MutableSharedFlow<String>()

    fun signUp(email: String, password: String, name: String) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                state.emit(CasualState.Loading)
                val user = ApiClient.signUp(SignUpData(email, password, name))
                val room = ApiClient.findGame()
                val roomId = room.uid
                ApiClient.webSocketEvents(roomUid = roomId).collect {
                    println("##### event collected - $it")
                    when (it) {
                        SocketEvents.Start -> {
                            state.emit(CasualState.Data(ApiClient.getRoom(roomId) to user))
                        }
                        else -> {}
                    }
                }
            }.onSuccess {

            }.onFailure {
                it.printStackTrace()
                state.emit(CasualState.Idle)
            }

        }
    }
}