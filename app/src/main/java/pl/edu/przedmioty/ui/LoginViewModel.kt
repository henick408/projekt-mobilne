package pl.edu.przedmioty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.edu.przedmioty.data.AuthRepository
import pl.edu.przedmioty.util.CatalogValidation

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val error = CatalogValidation.validateEmail(email) ?: CatalogValidation.validatePassword(password)
        if (error != null) {
            _state.value = AuthUiState(errorMessage = error)
            return
        }
        _state.value = AuthUiState(isLoading = true)
        authRepository.login(email, password) { result ->
            _state.value = AuthUiState(errorMessage = result.exceptionOrNull()?.readableMessage())
            if (result.isSuccess) onSuccess()
        }
    }

    private fun Throwable.readableMessage(): String =
        localizedMessage ?: "Wystąpił błąd. Spróbuj ponownie."

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel() as T
        }
    }
}