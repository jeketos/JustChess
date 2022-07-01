package game.api

import game.api.request.Credentials
import game.api.request.MoveRequest
import game.api.request.SignUpData
import game.api.request.TokenWithUser
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
    private const val SIGN_UP = "$PATH/v1/signUp"
    private const val LOGIN = "$PATH/v1/login"
    private const val FIND_USER = "$PATH/v1/user/"
    private const val FIND_GAME = "$PATH/v1/findGame"
    private const val ROOMS = "$PATH/v1/rooms/"
    private const val MOVE = "$PATH/v1/makeMove"
    private const val GIVE_UP = "$PATH/v1/giveUp/"

    private var token: String = ""

    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
        install(WebSockets)
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    token = ""
                }
            }
        }
    }

    private var webSocketClient: HttpClient? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val socketEvents: MutableSharedFlow<SocketEvents?> = MutableSharedFlow(1)

    suspend fun signUp(data: SignUpData): User =
        post<TokenWithUser, SignUpData>(SIGN_UP, data)
            .also { token = it.token }
            .user

    suspend fun login(data: Credentials): User =
        post<TokenWithUser, Credentials>(LOGIN, data)
            .also { token = it.token }
            .user

    suspend fun findUser(): User = get(FIND_USER)

    suspend fun findGame(): Room = get(FIND_GAME)

    suspend fun getActiveRooms(): List<Room> = get(ROOMS)

    suspend fun getRoom(uid: String): Room = get(ROOMS + uid)

    suspend fun makeMove(move: MoveRequest) {
        post<Room, MoveRequest>(MOVE, move)
    }

    suspend fun giveUp(): Room = get(GIVE_UP)

    fun webSocketEvents(roomUid: String): SharedFlow<SocketEvents?> {
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
                        headers {
                            this.append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    },
                    path = "/room/$roomUid"
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

    private suspend inline fun <reified T> get(
        path: String
    ): T = client.get(path) {
        println("##### append token - $token")
            if (token.isNotEmpty()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
    }.body()

    private suspend inline fun <reified T, reified B> post(
        path: String,
        body: B,
    ): T = client.post(path) {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        if (token.isNotEmpty()) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        setBody(body)
    }.body()
}