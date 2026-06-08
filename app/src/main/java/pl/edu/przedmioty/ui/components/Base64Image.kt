package pl.edu.przedmioty.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import pl.edu.przedmioty.util.ImageUtils

@Composable
fun Base64Image(
    encoded: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Zdjęcie przedmiotu",
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = remember(encoded) { ImageUtils.decodeBase64(encoded) }

    if (bitmap == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Brak zdjęcia", style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}