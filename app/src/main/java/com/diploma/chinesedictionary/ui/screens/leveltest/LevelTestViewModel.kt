package com.diploma.chinesedictionary.ui.screens.leveltest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.TestQuestion
import com.diploma.chinesedictionary.domain.repository.LevelTestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel входного HSK-теста.
 *
 * Реализует адаптивный алгоритм (по блок-схеме пользователя):
 *
 *   1. Начинаем с HSK 1
 *   2. Загружаем 10 вопросов этого уровня
 *   3. Пользователь отвечает на 10 вопросов
 *   4. Считаем % правильных
 *      - Если >= 60% И уровень < 6 → переходим на следующий уровень,
 *        загружаем 10 новых вопросов, GOTO 3
 *      - Если < 60% ИЛИ уровень = 6 → фиксируем уровень, показываем итог
 *   5. Пользователь видит результат и нажимает "Сохранить и выйти"
 *      → результат отправляется на сервер, профиль обновляется
 *
 * Состояние теста хранится локально, на сервер отправляется только финал.
 */
class LevelTestViewModel(
    private val repository: LevelTestRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelTestUiState())
    val uiState: StateFlow<LevelTestUiState> = _uiState.asStateFlow()

    /**
     * Старт теста — загружаем первые 10 вопросов HSK 1.
     */
    fun start() {
        _uiState.value = LevelTestUiState()
        loadQuestions(hskLevel = 1)
    }

    /** Перейти к следующему вопросу (или к подсчёту результата уровня). */
    fun submitAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val userAnswer = state.pendingAnswer
        if (userAnswer.isBlank()) return

        val isCorrect = question.isCorrect(userAnswer)

        // Обновляем счётчик ответов
        val newCorrect = state.correctOnCurrentLevel + (if (isCorrect) 1 else 0)
        val newAnswered = state.answeredOnCurrentLevel + 1

        // Переходим в фазу обратной связи (показать правильно/неправильно)
        _uiState.update {
            it.copy(
                phase = TestPhase.FEEDBACK,
                lastAnswerCorrect = isCorrect,
                correctOnCurrentLevel = newCorrect,
                answeredOnCurrentLevel = newAnswered,
            )
        }
    }

    /**
     * Перейти к следующему вопросу после показа обратной связи.
     * Либо к следующему уровню, либо к итогу.
     */
    fun nextQuestion() {
        val state = _uiState.value

        // Если ответили на все 10 вопросов уровня — проверяем алгоритм
        if (state.answeredOnCurrentLevel >= state.totalOnCurrentLevel) {
            advanceLevel()
            return
        }

        // Иначе — следующий вопрос текущего уровня
        _uiState.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                phase = TestPhase.QUESTION,
                pendingAnswer = "",
                lastAnswerCorrect = null,
            )
        }
    }

    /** Обновить ответ пользователя (для multiple choice или текстового ввода). */
    fun updateAnswer(answer: String) {
        _uiState.update { it.copy(pendingAnswer = answer) }
    }

    /**
     * Решает, что делать после 10 вопросов текущего уровня.
     * По блок-схеме:
     *   - >= 60% правильных И уровень < 6 → повысить уровень
     *   - иначе → фиксируем текущий уровень
     */
    private fun advanceLevel() {
        val state = _uiState.value
        val percentCorrect = if (state.totalOnCurrentLevel > 0) {
            state.correctOnCurrentLevel.toFloat() / state.totalOnCurrentLevel
        } else 0f

        // Записываем результат текущего уровня в общую статистику
        val updatedStats = state.answersPerLevel.toMutableMap().apply {
            this[state.currentHskLevel] =
                state.correctOnCurrentLevel to state.totalOnCurrentLevel
        }

        val shouldAdvance = percentCorrect >= PASS_THRESHOLD && state.currentHskLevel < MAX_HSK_LEVEL

        if (shouldAdvance) {
            // Переходим на следующий уровень — грузим новые вопросы
            val nextLevel = state.currentHskLevel + 1
            _uiState.update {
                it.copy(
                    answersPerLevel = updatedStats,
                    // Уровень сейчас сохраним как "потенциальный финальный" пока
                    // не загрузим следующие вопросы. Сразу после прохождения
                    // следующего уровня этот определённый сдвинется.
                )
            }
            loadQuestions(hskLevel = nextLevel)
        } else {
            // Фиксируем уровень. Если на первом уровне (HSK 1) не набрали 60% —
            // финальный уровень = 1 (т.е. "новичок", не прошёл HSK 1).
            // Если на HSK 3 не набрали 60% — финальный = 2 (предыдущий пройденный).
            // Если HSK 6 пройден — финальный = 6.
            //
            // Логика "максимальный пройденный уровень":
            //   - Если на HSK N не набрал 60% → пройденный = N-1
            //     (но не меньше 1, чтобы не было 0)
            //   - Если HSK 6 пройден на >=60% → 6
            val determined = when {
                state.currentHskLevel == MAX_HSK_LEVEL && percentCorrect >= PASS_THRESHOLD ->
                    MAX_HSK_LEVEL  // прошёл даже HSK 6
                state.currentHskLevel == 1 -> 1  // не прошёл HSK 1 — всё равно ставим 1 как минимум
                else -> state.currentHskLevel - 1  // не прошёл текущий, фиксируем предыдущий
            }.coerceAtLeast(1)

            _uiState.update {
                it.copy(
                    phase = TestPhase.FINISHED,
                    determinedLevel = determined,
                    answersPerLevel = updatedStats,
                )
            }
        }
    }

    /**
     * Сохранить финальный уровень на сервере и завершить тест.
     * onSuccess вызывается после успешного сохранения.
     */
    fun saveAndExit(onSuccess: () -> Unit) {
        val state = _uiState.value
        val finalLevel = state.determinedLevel ?: return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveResult(
                determinedLevel = finalLevel,
                answersPerLevel = state.answersPerLevel,
            )
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Не удалось сохранить результат",
                        )
                    }
                }
        }
    }

    private fun loadQuestions(hskLevel: Int) {
        _uiState.update {
            it.copy(
                phase = TestPhase.LOADING,
                currentHskLevel = hskLevel,
                currentQuestionIndex = 0,
                correctOnCurrentLevel = 0,
                answeredOnCurrentLevel = 0,
                pendingAnswer = "",
                lastAnswerCorrect = null,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            repository.getQuestions(hskLevel)
                .onSuccess { batch ->
                    _uiState.update {
                        it.copy(
                            phase = TestPhase.QUESTION,
                            currentQuestions = batch.questions,
                            totalOnCurrentLevel = batch.questions.size,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            phase = TestPhase.ERROR,
                            errorMessage = "Не удалось загрузить вопросы",
                        )
                    }
                }
        }
    }

    companion object {
        /** Проходной процент по блок-схеме пользователя. */
        const val PASS_THRESHOLD = 0.6f

        /** Максимальный HSK уровень. */
        const val MAX_HSK_LEVEL = 6
    }
}

enum class TestPhase {
    /** Стартовый экран ("Начать тест"). */
    INTRO,

    /** Загрузка вопросов с сервера. */
    LOADING,

    /** Показ вопроса, ожидание ответа пользователя. */
    QUESTION,

    /** Показ обратной связи (правильно/неправильно). */
    FEEDBACK,

    /** Финальный экран с результатом. */
    FINISHED,

    /** Ошибка — например, нет сети. */
    ERROR,
}

data class LevelTestUiState(
    val phase: TestPhase = TestPhase.INTRO,
    val currentHskLevel: Int = 1,
    val currentQuestions: List<TestQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val totalOnCurrentLevel: Int = 0,
    val correctOnCurrentLevel: Int = 0,
    val answeredOnCurrentLevel: Int = 0,
    val pendingAnswer: String = "",
    val lastAnswerCorrect: Boolean? = null,
    val determinedLevel: Int? = null,
    /** Маппа: HSK уровень → (правильно, всего) на этом уровне. */
    val answersPerLevel: Map<Int, Pair<Int, Int>> = emptyMap(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentQuestion: TestQuestion?
        get() = currentQuestions.getOrNull(currentQuestionIndex)

    /** Прогресс на текущем уровне (для прогресс-бара). */
    val levelProgress: Float
        get() = if (totalOnCurrentLevel == 0) 0f
        else answeredOnCurrentLevel.toFloat() / totalOnCurrentLevel
}
