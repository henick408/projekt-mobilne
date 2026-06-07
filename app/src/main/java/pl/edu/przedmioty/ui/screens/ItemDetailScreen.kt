package pl.edu.przedmioty.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.edu.przedmioty.ui.CatalogViewModel
import pl.edu.przedmioty.ui.ItemDetailViewModel

@Composable
fun ItemDetailScreen(
    catalogViewModel: CatalogViewModel,
    detailViewModel: ItemDetailViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Detail: $itemId")
    }
}