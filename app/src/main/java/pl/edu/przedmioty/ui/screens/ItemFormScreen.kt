package pl.edu.przedmioty.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.przedmioty.model.ItemDraft
import pl.edu.przedmioty.ui.CatalogViewModel
import pl.edu.przedmioty.ui.ItemFormViewModel
import pl.edu.przedmioty.ui.components.Base64Image
import pl.edu.przedmioty.util.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    catalogViewModel: CatalogViewModel,
    formViewModel: ItemFormViewModel,
    itemId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val catalogState by catalogViewModel.state.collectAsStateWithLifecycle()
    val formState by formViewModel.state.collectAsStateWithLifecycle()
    val existing = remember(itemId, catalogState.items) { itemId?.let { catalogViewModel.findItem(it) } }

    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category.orEmpty()) }
    var imageBase64 by remember(existing?.id) { mutableStateOf(existing?.imageBase64.orEmpty()) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingPhotoFile
        pendingPhotoFile = null
        if (!success || file == null) {
            file?.delete()
            photoError = "Nie wykonano zdjęcia."
        } else {
            ImageUtils.encodeAndDelete(file)
                .onSuccess {
                    imageBase64 = it
                    photoError = null
                }
                .onFailure { photoError = it.message ?: "Nie udało się przetworzyć zdjęcia." }
        }
    }

    fun openCamera() {
        val file = ImageUtils.createPrivateCameraFile(context)
        pendingPhotoFile = file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        takePictureLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera() else photoError = "Brak zgody na użycie aparatu."
    }

    fun requestPhoto() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Dodaj przedmiot" else "Edytuj przedmiot") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Wróć") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Base64Image(encoded = imageBase64, modifier = Modifier.fillMaxWidth().height(180.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = ::requestPhoto, modifier = Modifier.fillMaxWidth()) {
                Text(if (imageBase64.isBlank()) "Zrób zdjęcie" else "Zrób nowe zdjęcie")
            }
            photoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            formState.errors["image"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nazwa") },
                isError = formState.errors.containsKey("name"),
                supportingText = { formState.errors["name"]?.let { Text(it) } },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kategoria") },
                isError = formState.errors.containsKey("category"),
                supportingText = { formState.errors["category"]?.let { Text(it) } },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Opis") },
                minLines = 3,
                isError = formState.errors.containsKey("description"),
                supportingText = { formState.errors["description"]?.let { Text(it) } },
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    formViewModel.saveItem(
                        itemId = itemId,
                        draft = ItemDraft(name, description, category, imageBase64),
                        existingItem = existing,
                        onSuccess = onSaved,
                    )
                },
                enabled = !formState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (formState.isSaving) CircularProgressIndicator() else Text("Zapisz")
            }
            formState.errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}