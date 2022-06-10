package game.ui.auth.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import game.navigation.NavController
import game.navigation.NavDestination

@Composable
fun Splash(
    navController: NavController,
    viewModel: SplashViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.state.collect { (room, user) ->
            navController.navigate(NavDestination.OnlinePlay(room, user))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Button(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                onClick = { viewModel.findGame() }
            ) {
                Text("Sign up & Find Game")
            }
            Button(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                onClick = { navController.navigate(NavDestination.Login) }
            ) {
                Text("Login")
            }
            Button(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                onClick = { navController.navigate(NavDestination.HotSeat) }
            ) {
                Text("Hot Seat")
            }
        }
    }
}