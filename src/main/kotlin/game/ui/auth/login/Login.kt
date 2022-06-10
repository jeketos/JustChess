package game.ui.auth.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import game.navigation.NavController
import game.navigation.NavDestination

@Composable
fun Login(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val text = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.state.collect { (room, user) ->
            navController.navigate(NavDestination.OnlinePlay(room, user))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                value = text.value,
                onValueChange = {
                    text.value = it
                }
            )
            Button(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                onClick = {
                    viewModel.signIn(text.value)
                }
            ) {
                Text("Sign in & find game")
            }
        }
    }
}