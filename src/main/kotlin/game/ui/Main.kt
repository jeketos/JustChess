package game.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import game.navigation.NavController
import game.navigation.NavDestination
import game.ui.auth.login.Login
import game.ui.auth.login.LoginViewModel
import game.ui.auth.splash.Splash
import game.ui.auth.splash.SplashViewModel
import game.ui.hotseat.HotSeat
import game.ui.hotseat.HotSeatGameViewModel
import game.ui.online.OnlineGame
import game.ui.online.OnlineGameViewModel

fun main() = application {
    val navController = NavController(NavDestination.Splash)
    Window(onCloseRequest = ::exitApplication) {
        val destination by navController.currentDestination.collectAsState()
        when (val d = destination) {
            NavDestination.Splash -> Splash(navController = navController, viewModel = SplashViewModel())
            NavDestination.HotSeat -> HotSeat(viewModel = HotSeatGameViewModel(), navController = navController)
            NavDestination.Login -> Login(navController = navController, viewModel = LoginViewModel())
            is NavDestination.OnlinePlay -> OnlineGame(navController = navController, viewModel = OnlineGameViewModel(d.data, d.user))
        }
    }
}
