package pl.edu.przedmioty.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.edu.przedmioty.ui.AppViewModel

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Login screen – coming soon")
    }
}