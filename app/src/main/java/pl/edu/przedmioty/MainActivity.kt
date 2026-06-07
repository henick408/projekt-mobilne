package pl.edu.przedmioty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import pl.edu.przedmioty.ui.ItemCatalogApp
import pl.edu.przedmioty.ui.theme.PrzedmiotyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrzedmiotyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ItemCatalogApp()
                }
            }
        }
    }
}