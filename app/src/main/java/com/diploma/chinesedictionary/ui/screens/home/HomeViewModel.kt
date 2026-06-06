package com.diploma.chinesedictionary.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.User
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * ViewModel главного экрана.
 *
 * Загружает данные параллельно:
 *   1. Профиль пользователя (для приветствия с именем)
 *   2. Случайное слово дня
 *
 * Параллельная загрузка через async/awaitAll — это идиоматичный паттерн
 * корутин для нескольких независимых асинхронных операций. Если делать
 * последовательно (один await, потом второй), время равно сумме. Если
 * параллельно — время равно максимуму. Для главного экрана это критично:
 * пользователь не должен ждать.
 *
 * Параметр authRepository нужен только для получения профиля. Когда
 * добавим эндпоинт прогресса, добавим ProgressRepository аналогично.
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    /**
     * Перезагрузка всех данных. Используется при pull-to-refresh
     * или кнопке "обновить".
     */
    fun refresh() {
        loadAll()
    }

    /**
     * Перезагружает только слово дня (например, по кнопке "ещё одно").
     */
    fun reloadWordOfTheDay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isReloadingWord = true) }
            dictionaryRepository.getRandomWord()
                .onSuccess { word ->
                    _uiState.update {
                        it.copy(wordOfTheDay = word, isReloadingWord = false)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isReloadingWord = false) }
                }
        }
    }

    private fun loadAll() {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                greeting = getGreetingForTime(),  // обновляем приветствие
            )
        }

        viewModelScope.launch {
            // Параллельная загрузка: запускаем оба запроса одновременно
            // через async {}. await() блокирует, но только в этой корутине —
            // UI продолжает работать. Если запускать последовательно, время
            // равно сумме; через async/await — равно максимуму. Для двух
            // запросов по ~100мс выигрыш ~50%.
            val userDeferred = async { authRepository.getCurrentUser() }
            val wordDeferred = async { dictionaryRepository.getRandomWord() }

            val user = userDeferred.await().getOrNull()
            val word = wordDeferred.await().getOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    wordOfTheDay = word,
                    error = if (user == null && word == null) {
                        "Не удалось загрузить данные. Проверьте подключение."
                    } else null,
                )
            }
        }
    }

    /**
     * Возвращает приветствие в зависимости от времени суток.
     * На китайском есть отдельные приветствия для утра, дня, вечера и ночи.
     */
    private fun getGreetingForTime(): Greeting {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> Greeting(
                chinese = "早上好",
                russian = "Доброе утро",
                pinyin = "zǎo shàng hǎo",
            )
            in 12..17 -> Greeting(
                chinese = "下午好",
                russian = "Добрый день",
                pinyin = "xià wǔ hǎo",
            )
            in 18..22 -> Greeting(
                chinese = "晚上好",
                russian = "Добрый вечер",
                pinyin = "wǎn shàng hǎo",
            )
            else -> Greeting(
                chinese = "晚安",
                russian = "Доброй ночи",
                pinyin = "wǎn ān",
            )
        }
    }
}

/**
 * Состояние главного экрана.
 *
 * Несколько секций, которые загружаются независимо. UI отображает
 * каждую секцию со своим состоянием (загрузка/данные/ошибка).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isReloadingWord: Boolean = false,
    val greeting: Greeting = Greeting("你好", "Здравствуй", "nǐ hǎo"),
    val user: User? = null,
    val wordOfTheDay: WordDetail? = null,
    val error: String? = null,
)

/**
 * Приветствие в трёх формах.
 */
data class Greeting(
    val chinese: String,
    val russian: String,
    val pinyin: String,
)
