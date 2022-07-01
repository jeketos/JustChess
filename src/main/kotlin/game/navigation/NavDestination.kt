package game.navigation

import game.api.response.Room
import game.api.response.User

sealed class NavDestination {
    object Splash: NavDestination()
    object HotSeat : NavDestination()
    object Login : NavDestination()
    object SignUp : NavDestination()
    class OnlinePlay(val data: Room, val user: User) : NavDestination()
}