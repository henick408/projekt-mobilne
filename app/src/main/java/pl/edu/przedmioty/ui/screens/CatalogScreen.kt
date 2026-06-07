package pl.edu.przedmioty.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.edu.przedmioty.ui.AppViewModel

@Composable
fun CatalogScreen(
    viewModel: AppViewModel,
    onOpenItem: (String) -> Unit,
    onAddItem: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}