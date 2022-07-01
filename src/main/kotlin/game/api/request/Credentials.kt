package game.api.request

import game.api.response.User
import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val email: String,
    val password: String
)

@Serializable
data class SignUpData(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class TokenWithUser(
    val token: String,
    val user: User
)