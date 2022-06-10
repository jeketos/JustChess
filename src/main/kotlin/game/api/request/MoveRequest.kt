package game.api.request

import kotlinx.serialization.Serializable

@Serializable
data class MoveRequest(
    val userUid: String,
    val roomUid: String,
    val board: String
)
