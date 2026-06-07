package pl.edu.przedmioty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.edu.przedmioty.data.AuthRepository
import pl.edu.przedmioty.data.ItemRepository
import pl.edu.przedmioty.model.CatalogItem

data class CatalogUiState(
    val items: List<CatalogItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class CatalogViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening() {
        val userId = authRepository.currentUserId ?: return
        listenerRegistration?.remove()
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        listenerRegistration = itemRepository.listen(
            userId = userId,
            onItemsChanged = { items ->
                _state.update { it.copy(items = items, isLoading = false, errorMessage = null) }
            },
            onError = { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.readableMessage()) }
            },
        )
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun findItem(itemId: String): CatalogItem? =
        _state.value.items.firstOrNull { it.id == itemId }

    fun logout() {
        stopListening()
        authRepository.logout()
        _state.value = CatalogUiState()
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }

    private fun Throwable.readableMessage(): String =
        localizedMessage ?: "Wystąpił błąd. Spróbuj ponownie."

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel() as T
        }
    }
}