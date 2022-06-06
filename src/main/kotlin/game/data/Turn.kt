package game.data

data class Turn(
    val color: GameColor,
    val gameCondition: GameCondition = GameCondition.NothingSpecial
)
