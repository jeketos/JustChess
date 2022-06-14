package game.ui.auth.splash

import game.api.ApiClient
import game.api.response.Room
import game.api.response.User
import game.api.response.list
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    val state = MutableSharedFlow<Pair<Room, User>>()

    fun findGame() {
        scope.launch(Dispatchers.IO) {
            //TODO add socket impl
            runCatching {
                val user = ApiClient.signUp()
                val room = ApiClient.findGame(user.uid)
                val roomId = room.uid
                val result = if (room.players.list().size == 2) {
                    room
                } else {
                    repeat(roomId)
                }
                state.emit(result to user)
            }.onSuccess {

            }.onFailure {
                it.printStackTrace()
                println("failure")
            }

        }
    }

    private suspend fun repeat(uid: String): Room {
        delay(1000)
        val room = ApiClient.getRoom(uid)
        return if (room.players.list().size != 2) repeat(uid)
        else room
    }
}