package pl.edu.przedmioty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.przedmioty.model.CatalogItem
import pl.edu.przedmioty.ui.CatalogViewModel
import pl.edu.przedmioty.ui.components.Base64Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onOpenItem: (String) -> Unit,
    onAddItem: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mój katalog") },
                actions = {
                    TextButton(onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    }) { Text("Wyloguj") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) { Text("+") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            when {
                state.isLoading -> CircularProgressIndicator()
                state.items.isEmpty() -> Text("Katalog jest pusty. Dodaj pierwszy przedmiot.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.items, key = { it.id }) { item ->
                        CatalogItemCard(item = item, onClick = { onOpenItem(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(item: CatalogItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Base64Image(encoded = item.imageBase64, modifier = Modifier.size(72.dp))
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(item.category, style = MaterialTheme.typography.labelMedium)
                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}