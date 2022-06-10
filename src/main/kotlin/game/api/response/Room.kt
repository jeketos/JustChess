package game.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Room(
    val uid: String,
    val players: Players,
    val gameState: GameState
)

@Serializable
data class Players(
    val eagle: User? = null,
    var tail: User? = null
)

fun Players.list() = listOfNotNull(eagle, tail)


@Serializable
data class GameState(
    val turn: String, // player UID
    val movesCount: Int,
    val board: String,
    val state: State,
    val winner: String?
)

enum class State {
    Idle,
    Check,
    Stalemate,
    Mate
}
