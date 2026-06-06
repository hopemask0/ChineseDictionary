package com.diploma.chinesedictionary.ui.screens.trainer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.TrainerCard
import com.diploma.chinesedictionary.domain.repository.ProgressRepository
import com.diploma.chinesedictionary.ui.navigation.NavArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel тренировки по списку.
 *
 * Жизненный цикл сессии:
 *   1. init {} — грузим карточки с сервера
 *   2. flipCard() — пользователь нажал "Показать ответ"
 *   3. answer(knows) — пользователь нажал "Знаю" / "Учу"
 *      - Отправляет ответ на сервер (асинхронно — не блокирует UI)
 *      - Переходит к следующей карточке
 *   4. Когда currentIndex == cards.size — сессия завершена,
 *      isFinished = true, показывается экран итога
 *
 * Карточки перемешиваются один раз при загрузке (random.shuffled()) —
 * чтобы при повторной тренировке порядок отличался.
 */
class TrainerViewModel(
    private val repository: ProgressRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val listId: Int = savedStateHandle.get<Int>(NavArgs.LIST_ID)
        ?: error("TrainerViewModel requires listId argument")

    private val _uiState = MutableStateFlow(TrainerUiState(isLoading = true))
    val uiState: StateFlow<TrainerUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    /** Перевернуть карточку (показать ответ). */
    fun flipCard() {
        _uiState.update { it.copy(showAnswer = true) }
    }

    /**
     * Записать ответ пользователя и перейти к следующей карточке.
     * Ответ отправляется на сервер в фоне — UI не ждёт.
     */
    fun answer(knows: Boolean) {
        val state = _uiState.value
        val currentCard = state.cards.getOrNull(state.currentIndex) ?: return

        // Отправляем на сервер в фоне (не блокируем UI)
        viewModelScope.launch {
            repository.recordAnswer(currentCard.wordId, knows)
        }

        // Обновляем локальную статистику и идём дальше
        val newKnown = state.knownInSession + (if (knows) 1 else 0)
        val newLearning = state.learningInSession + (if (!knows) 1 else 0)
        val nextIndex = state.currentIndex + 1
        val isFinished = nextIndex >= state.cards.size

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                showAnswer = false,
                knownInSession = newKnown,
                learningInSession = newLearning,
                isFinished = isFinished,
            )
        }
    }

    /** Начать тренировку заново (после показа итогового экрана). */
    fun restart() {
        loadSession()
    }

    private fun loadSession() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getSession(listId)
                .onSuccess { session ->
                    _uiState.update {
                        TrainerUiState(
                            isLoading = false,
                            listTitle = session.listTitle,
                            // Перемешиваем — каждая тренировка с разным порядком
                            cards = session.cards.shuffled(),
                            currentIndex = 0,
                            showAnswer = false,
                            knownInSession = 0,
                            learningInSession = 0,
                            isFinished = session.cards.isEmpty(),
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Не удалось загрузить тренировку")
                    }
                }
        }
    }
}

data class TrainerUiState(
    val isLoading: Boolean = false,
    val listTitle: String = "",
    val cards: List<TrainerCard> = emptyList(),
    val currentIndex: Int = 0,
    val showAnswer: Boolean = false,
    val knownInSession: Int = 0,
    val learningInSession: Int = 0,
    val isFinished: Boolean = false,
    val error: String? = null,
) {
    val currentCard: TrainerCard?
        get() = cards.getOrNull(currentIndex)

    /** Прогресс от 0.0 до 1.0 для прогресс-бара. */
    val progress: Float
        get() = if (cards.isEmpty()) 0f else currentIndex.toFloat() / cards.size
}
