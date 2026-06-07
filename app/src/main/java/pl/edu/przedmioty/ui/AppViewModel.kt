package pl.edu.przedmioty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.edu.przedmioty.data.AuthRepository
import pl.edu.przedmioty.data.ItemRepository
import pl.edu.przedmioty.model.CatalogItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.edu.przedmioty.util.CatalogValidation

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class CatalogUiState(
    val items: List<CatalogItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class AppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _catalogState = MutableStateFlow(CatalogUiState())
    val catalogState: StateFlow<CatalogUiState> = _catalogState.asStateFlow()

    fun isSignedIn(): Boolean = authRepository.isSignedIn()

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            _authState.value = AuthUiState(errorMessage = validationError)
            return
        }
        _authState.value = AuthUiState(isLoading = true)
        authRepository.register(email, password) { result ->
            _authState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.readableMessage())
            if (result.isSuccess) onSuccess()
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            _authState.value = AuthUiState(errorMessage = validationError)
            return
        }
        _authState.value = AuthUiState(isLoading = true)
        authRepository.login(email, password) { result ->
            _authState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.readableMessage())
            if (result.isSuccess) onSuccess()
        }
    }

    fun logout() {
        authRepository.logout()
        _catalogState.value = CatalogUiState()
        _authState.value = AuthUiState()
    }

    private fun validateCredentials(email: String, password: String): String? =
        CatalogValidation.validateEmail(email)
            ?: CatalogValidation.validatePassword(password)

    private fun Throwable.readableMessage(): String =
        localizedMessage ?: "Wystąpił błąd. Spróbuj ponownie."

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel() as T
        }
    }
}