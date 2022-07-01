package game.api.request

import kotlinx.serialization.Serializable

@Serializable
data class MoveRequest(
    val roomUid: String,
    val board: String
)
