package pl.edu.przedmioty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.przedmioty.ui.CatalogViewModel
import pl.edu.przedmioty.ui.ItemDetailViewModel
import pl.edu.przedmioty.ui.components.Base64Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    catalogViewModel: CatalogViewModel,
    detailViewModel: ItemDetailViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val catalogState by catalogViewModel.state.collectAsStateWithLifecycle()
    val deleteState by detailViewModel.deleteState.collectAsStateWithLifecycle()
    val item = catalogState.items.firstOrNull { it.id == itemId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Wróć") } },
            )
        },
    ) { padding ->
        if (item == null) {
            Text("Nie znaleziono przedmiotu.", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (!showDialog) {
                Base64Image(
                    encoded = item.imageBase64,
                    modifier = Modifier.fillMaxSize().height(180.dp).clickable{showDialog = true},
                    contentScale = ContentScale.Fit,
                )
            }
            else {
                Dialog(
                    onDismissRequest = {showDialog = false},
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    Base64Image(
                        encoded = item.imageBase64,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(item.name, style = MaterialTheme.typography.headlineSmall)
            Text("Kategoria: ${item.category}", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            Text(if (item.description.isBlank()) "Brak opisu" else item.description)
            Spacer(Modifier.height(20.dp))
            Row {
                Button(onClick = onEdit, enabled = !deleteState.isDeleting) { Text("Edytuj") }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !deleteState.isDeleting,
                    modifier = Modifier.padding(start = 12.dp),
                ) { Text("Usuń") }
            }
            deleteState.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usunąć przedmiot?") },
            text = { Text("Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    detailViewModel.deleteItem(itemId, onDeleted)
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            },
        )
    }
}