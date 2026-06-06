package com.diploma.chinesedictionary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Главный ViewModel приложения.
 *
 * Определяет, что показать при запуске:
 *   - SPLASH      — кратко, пока идёт проверка
 *   - ONBOARDING  — пользователь не залогинен
 *   - MAIN        — пользователь залогинен, показываем основное приложение
 *
 * Этот ViewModel живёт на уровне MainActivity и переживает навигацию
 * внутри приложения. Onboarding и MAIN — две "вселенные", между которыми
 * мы переключаемся.
 */
class MainViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Проверяем при старте: есть ли локально сохранённый пользователь
     * И валиден ли он на сервере (т.е. сервер его знает).
     *
     * Логика:
     *   1. Если локально пусто → ONBOARDING
     *   2. Если локально есть user_id, делаем /auth/me для проверки
     *      - 200 OK → MAIN (всё хорошо, профиль обновили из ответа)
     *      - 404 → сессия очищается автоматически в Repository → ONBOARDING
     *      - другая ошибка (нет сети) → всё равно MAIN, работаем с локальными
     *        данными; пользователь не должен застрять на splash, если сервер
     *        временно недоступен
     */
    private fun checkAuthStatus() {
        if (!authRepository.isLoggedIn()) {
            _uiState.update { it.copy(state = AppState.ONBOARDING) }
            return
        }

        // Локально залогинен, проверяем актуальность на сервере
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess {
                    _uiState.update { it.copy(state = AppState.MAIN) }
                }
                .onFailure {
                    // Если 401/404 — Repository уже очистил сессию.
                    // Перепроверяем флаг и переходим в нужное состояние.
                    val nextState = if (authRepository.isLoggedIn()) {
                        AppState.MAIN  // сетевая ошибка, но локально мы залогинены
                    } else {
                        AppState.ONBOARDING  // сессия сбросилась
                    }
                    _uiState.update { it.copy(state = nextState) }
                }
        }
    }

    /**
     * Вызывается после успешной регистрации в онбординге.
     */
    fun onOnboardingComplete() {
        _uiState.update { it.copy(state = AppState.MAIN) }
    }

    /**
     * Вызывается, когда пользователь выходит из профиля.
     */
    fun onLogout() {
        authRepository.logout()
        _uiState.update { it.copy(state = AppState.ONBOARDING) }
    }
}

enum class AppState {
    SPLASH,
    ONBOARDING,
    MAIN,
}

data class MainUiState(
    val state: AppState = AppState.SPLASH,
)
