package game.data

sealed class CasualState<out T> {
    object Idle: CasualState<Nothing>()
    object Loading: CasualState<Nothing>()
    class  Data<T>(val data: T): CasualState<T>()
}