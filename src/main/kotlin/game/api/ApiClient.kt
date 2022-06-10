package game.api

import game.api.request.MoveRequest
import game.api.response.Room
import game.api.response.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    private const val PATH = "http://0.0.0.0:8080/api"
    private const val JOIN = "$PATH/v1/join"
    private const val FIND_USER = "$PATH/v1/user/"
    private const val FIND_GAME = "$PATH/v1/findGame/"
    private const val ROOMS = "$PATH/v1/rooms/"
    private const val MOVE = "$PATH/v1/makeMove"
    private const val GIVE_UP = "$PATH/v1/giveUp/"

    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun signUp(): User = client.get(JOIN).body()

    suspend fun findUser(userUid: String): User = client.get(FIND_USER + userUid).body()

    suspend fun findGame(userUid: String): Room = client.get(FIND_GAME + userUid).body()

    suspend fun getActiveRooms(): List<Room> = client.get(ROOMS).body()

    suspend fun getRoom(uid: String): Room = client.get(ROOMS + uid).body()

    suspend fun makeMove(move: MoveRequest): Room = client.post(MOVE) {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(move)
    }.body()

    suspend fun giveUp(userUid: String):Room = client.get(GIVE_UP + userUid).body()
}