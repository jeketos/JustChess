package game.ui.auth.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import game.navigation.NavController
import game.navigation.NavDestination
import game.ui.components.LoadingButton

@Composable
fun Splash(
    navController: NavController,
    viewModel: SplashViewModel
) {
    val state by viewModel.state.collectAsState(null)
    LaunchedEffect(Unit) {
        viewModel.state.collect { state ->
            if (state is SplashState.Success) {
                navController.navigate(NavDestination.OnlinePlay(state.room, state.user))
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            LoadingButton(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                text = "Sign up & Find Game",
                isLoading = state == SplashState.FindGame,
                onClick = { viewModel.findGame() }
            )
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