package game.data

sealed class GameCondition {
    object NothingSpecial: GameCondition()
    object Check: GameCondition()
    object Mate: GameCondition()
    object Stalemate: GameCondition()
}