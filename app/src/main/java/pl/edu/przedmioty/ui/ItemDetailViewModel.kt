package pl.edu.przedmioty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.edu.przedmioty.data.AuthRepository
import pl.edu.przedmioty.data.ItemRepository

data class ItemDeleteUiState(
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
)

class ItemDetailViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {
    private val _deleteState = MutableStateFlow(ItemDeleteUiState())
    val deleteState: StateFlow<ItemDeleteUiState> = _deleteState.asStateFlow()

    fun deleteItem(itemId: String, onSuccess: () -> Unit) {
        val userId = authRepository.currentUserId ?: return
        _deleteState.update { it.copy(isDeleting = true, errorMessage = null) }
        itemRepository.delete(userId, itemId) { result ->
            _deleteState.update {
                it.copy(isDeleting = false, errorMessage = result.exceptionOrNull()?.readableMessage())
            }
            if (result.isSuccess) onSuccess()
        }
    }

    private fun Throwable.readableMessage(): String =
        localizedMessage ?: "Wystąpił błąd. Spróbuj ponownie."

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ItemDetailViewModel() as T
        }
    }
}