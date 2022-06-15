package game.data

enum class SocketEvents(val id: String) {
    Start("Start"),
    Update("Update"),
    Close("Close")
}

fun String.toSocketEvents() = SocketEvents.values().firstOrNull { it.id == this }