package pl.edu.przedmioty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.edu.przedmioty.data.AuthRepository
import pl.edu.przedmioty.data.ItemRepository
import pl.edu.przedmioty.model.CatalogItem
import pl.edu.przedmioty.model.ItemDraft
import pl.edu.przedmioty.util.CatalogValidation

data class ItemFormUiState(
    val isSaving: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
)

class ItemFormViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(ItemFormUiState())
    val state: StateFlow<ItemFormUiState> = _state.asStateFlow()

    fun saveItem(
        itemId: String?,
        draft: ItemDraft,
        existingItem: CatalogItem?,
        onSuccess: () -> Unit,
    ) {
        val userId = authRepository.currentUserId ?: return
        val errors = CatalogValidation.validateItem(draft)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(isSaving = true, errors = emptyMap(), errorMessage = null) }
        val now = System.currentTimeMillis()
        val callback: (Result<Unit>) -> Unit = { result ->
            _state.update {
                it.copy(isSaving = false, errorMessage = result.exceptionOrNull()?.readableMessage())
            }
            if (result.isSuccess) onSuccess()
        }
        if (itemId == null) {
            val newId = itemRepository.newDocumentId(userId)
            itemRepository.create(
                userId,
                CatalogItem(
                    id = newId,
                    name = draft.name.trim(),
                    description = draft.description.trim(),
                    category = draft.category.trim(),
                    imageBase64 = draft.imageBase64,
                    createdAt = now,
                    updatedAt = now,
                ),
                callback,
            )
        } else {
            if (existingItem == null) {
                _state.update { it.copy(isSaving = false, errorMessage = "Nie znaleziono przedmiotu.") }
                return
            }
            itemRepository.update(
                userId,
                existingItem.copy(
                    name = draft.name.trim(),
                    description = draft.description.trim(),
                    category = draft.category.trim(),
                    imageBase64 = draft.imageBase64,
                    updatedAt = now,
                ),
                callback,
            )
        }
    }

    private fun Throwable.readableMessage(): String =
        localizedMessage ?: "Wystąpił błąd. Spróbuj ponownie."

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ItemFormViewModel() as T
        }
    }
}