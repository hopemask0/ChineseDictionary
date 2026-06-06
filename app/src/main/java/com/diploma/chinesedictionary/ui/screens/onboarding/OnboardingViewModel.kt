package com.diploma.chinesedictionary.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.Level
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel онбординга — мастера из 3 шагов:
 *   1. Имя (username + display_name)
 *   2. Цели изучения (можно выбрать несколько)
 *   3. Уровень (radio: новичок / 1-2 / 3-4 / 5-6 / пройти тест)
 *
 * Один ViewModel на все три экрана. Так состояние не теряется при
 * переходе между шагами — пользователь может вернуться назад и не
 * потерять введённое имя.
 *
 * Альтернатива — отдельный ViewModel на каждый шаг + передача данных
 * через NavController. Но это сложнее и для онбординга избыточно.
 */
class OnboardingViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * Состояние всего онбординга.
     *
     * _uiState (с подчёркиванием) — приватный mutable StateFlow.
     * Менять состояние можно только изнутри ViewModel через _uiState.update {}.
     *
     * uiState (без подчёркивания) — публичная "только для чтения" версия,
     * которая отдаётся UI-слою. Compose подписывается на неё.
     *
     * Этот паттерн — лучшая практика в Kotlin: внутри гибкость, снаружи
     * иммутабельность.
     */
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // ===== Шаг 1: Имя =====

    fun onUsernameChange(value: String) {
        // Очищаем имя от пробелов, ограничиваем длину
        val cleaned = value.trim().take(50)
        _uiState.update {
            it.copy(
                username = cleaned,
                usernameError = validateUsername(cleaned),
            )
        }
    }

    fun onDisplayNameChange(value: String) {
        val trimmed = value.take(100)
        _uiState.update {
            it.copy(
                displayName = trimmed,
                displayNameError = if (trimmed.isBlank()) "Введите имя" else null,
            )
        }
    }

    private fun validateUsername(username: String): String? = when {
        username.isBlank() -> "Введите логин"
        username.length < 3 -> "Не короче 3 символов"
        !username.all { it.isLetterOrDigit() || it == '_' } ->
            "Только латиница, цифры и _"
        else -> null
    }

    // ===== Шаг 2: Цели =====

    /**
     * Переключение цели — если выбрана, убираем; если нет, добавляем.
     * Это паттерн "toggle" для чекбоксов.
     */
    fun onGoalToggle(goal: Goal) {
        _uiState.update { state ->
            val newGoals = if (goal in state.selectedGoals) {
                state.selectedGoals - goal
            } else {
                state.selectedGoals + goal
            }
            state.copy(selectedGoals = newGoals)
        }
    }

    // ===== Шаг 3: Уровень =====

    fun onLevelSelect(level: Level?) {
        // level = null означает "хочу пройти тест"
        _uiState.update { it.copy(selectedLevel = level, wantsToTakeTest = false) }
    }

    fun onWantsToTakeTestToggle() {
        _uiState.update {
            it.copy(
                wantsToTakeTest = !it.wantsToTakeTest,
                selectedLevel = if (!it.wantsToTakeTest) null else it.selectedLevel,
            )
        }
    }

    // ===== Финальная отправка =====

    /**
     * Отправляет данные на сервер и регистрирует пользователя.
     *
     * viewModelScope — это специальный CoroutineScope, привязанный к
     * жизненному циклу ViewModel. Если пользователь закрыл экран в
     * процессе регистрации, корутина автоматически отменится — лишних
     * запросов не будет.
     *
     * onSuccess вызывается после успеха для перехода на главный экран.
     */
    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Проверка полей перед отправкой
        if (state.username.isBlank() || state.usernameError != null) return
        if (state.displayName.isBlank() || state.displayNameError != null) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            // level = null если выбрано "пройти тест" или ничего не выбрано
            val levelCode = if (state.wantsToTakeTest) null else state.selectedLevel?.code

            authRepository.register(
                username = state.username,
                displayName = state.displayName,
                level = levelCode,
                goals = state.selectedGoals.toList(),
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = error.toUserMessage(),
                        )
                    }
                }
        }
    }

    private fun Throwable.toUserMessage(): String {
        val msg = message.orEmpty().lowercase()
        return when {
            "409" in msg || "already" in msg ->
                "Этот логин уже занят. Выберите другой."
            "timeout" in msg ->
                "Сервер не отвечает. Попробуйте позже."
            "connect" in msg || "unable to resolve" in msg ->
                "Не удалось подключиться к серверу."
            else -> "Не удалось зарегистрироваться: ${message ?: "неизвестная ошибка"}"
        }
    }
}

/**
 * Состояние онбординга.
 *
 * Хранит данные всех трёх шагов одновременно. Это data class — Kotlin
 * автоматически генерирует equals(), hashCode(), copy() и т.д.
 * Через copy() мы создаём новый State при каждом изменении, не мутируя
 * существующий — это важно для корректной работы Compose с State.
 */
data class OnboardingUiState(
    // Шаг 1
    val username: String = "",
    val displayName: String = "",
    val usernameError: String? = null,
    val displayNameError: String? = null,

    // Шаг 2
    val selectedGoals: Set<Goal> = emptySet(),

    // Шаг 3
    val selectedLevel: Level? = null,
    val wantsToTakeTest: Boolean = false,

    // Финал
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)
