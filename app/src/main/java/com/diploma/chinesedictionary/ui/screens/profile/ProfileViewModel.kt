package com.diploma.chinesedictionary.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.User
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана профиля.
 *
 * Загружает данные текущего пользователя через /auth/me и показывает их.
 * Также управляет кнопкой "Выйти".
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    fun retry() {
        loadUser()
    }

    private fun loadUser() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(isLoading = false, user = user, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось загрузить профиль",
                        )
                    }
                }
        }
    }

    fun logout() {
        authRepository.logout()
        // Сам переход на онбординг произойдёт через MainViewModel,
        // мы лишь триггерим его через флаг.
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
)
