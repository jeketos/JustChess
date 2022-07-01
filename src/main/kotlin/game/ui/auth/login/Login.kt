package game.ui.auth.login

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import game.data.CasualState
import game.navigation.NavController
import game.navigation.NavDestination
import game.ui.components.LoadingButton

@Composable
fun Login(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val viewState by viewModel.state.collectAsState(CasualState.Idle)
    val email = remember { mutableStateOf("v@v.v") }
    val password = remember { mutableStateOf("111111") }
    LaunchedEffect(Unit) {
        viewModel.state.collect { state ->
            if (state is CasualState.Data) {
                navController.popBackStack()
                navController.navigate(NavDestination.OnlinePlay(state.data.first, state.data.second))
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                label = { Text("Email") },
                value = email.value,
                onValueChange = {
                    email.value = it
                }
            )
            OutlinedTextField(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                label = { Text("Password") },
                value = password.value,
                onValueChange = {
                    password.value = it
                }
            )
            LoadingButton(
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                onClick = {
                    viewModel.signIn(email.value, password.value)
                },
                isLoading = viewState == CasualState.Loading,
                text = "Sign in & find game"
            )
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    Login(NavController(NavDestination.Login), LoginViewModel())
}