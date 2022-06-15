package game.api

import game.api.request.MoveRequest
import game.api.response.Room
import game.api.response.User
import game.data.SocketEvents
import game.data.toSocketEvents
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

object ApiClient {

    private const val HOST = "0.0.0.0"
    private const val PORT = 8080
    private const val PROTOCOL = "http://"
    private const val PATH = "$PROTOCOL$HOST:$PORT/api"
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
        install(WebSockets)
    }

    private var webSocketClient: HttpClient? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val socketEvents: MutableSharedFlow<SocketEvents?> = MutableSharedFlow(1)

    suspend fun signUp(): User = client.get(JOIN).body()

    suspend fun findUser(userUid: String): User = client.get(FIND_USER + userUid).body()

    suspend fun findGame(userUid: String): Room = client.get(FIND_GAME + userUid).body()

    suspend fun getActiveRooms(): List<Room> = client.get(ROOMS).body()

    suspend fun getRoom(uid: String): Room = client.get(ROOMS + uid).body()

    suspend fun makeMove(move: MoveRequest) {
        client.post(MOVE) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(move)
        }
    }

    suspend fun giveUp(userUid: String): Room = client.get(GIVE_UP + userUid).body()

    fun webSocketEvents(roomUid: String, userUid: String): SharedFlow<SocketEvents?> {
        scope.launch {
            if (webSocketClient == null || webSocketClient?.isActive == false) {
                webSocketClient = HttpClient(CIO) {
                    install(WebSockets)
                    install(HttpTimeout)
                }
                webSocketClient?.webSocket(
                    method = HttpMethod.Get,
                    host = HOST,
                    port = PORT,
                    request = {
                        timeout { requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS }
                    },
                    path = "/room/$roomUid/user/$userUid"
                ) {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val value = frame.readText()
                        println("##### event - $value")
                        socketEvents.emit(value.toSocketEvents())
                    }
                }
            }
        }
        return socketEvents
    }

    fun closeWebSocket() {
        webSocketClient?.close()
        webSocketClient = null
    }
}