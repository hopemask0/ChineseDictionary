package com.diploma.chinesedictionary.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.WordSummary
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана поиска.
 *
 * Принципы MVVM в Compose:
 *   - View (SearchScreen) ничего не знает о репозитории, сети, БД.
 *   - View подписывается на State из ViewModel и отображает его.
 *   - View передаёт пользовательские события (ввод текста, клик) обратно в
 *     ViewModel через коллбэки.
 *
 * StateFlow vs LiveData:
 *   Используем StateFlow — это современный Kotlin-flow с состоянием.
 *   В Compose StateFlow читается через collectAsStateWithLifecycle() и
 *   автоматически вызывает recomposition при изменении.
 *
 * Внедрение зависимостей: репозиторий передаётся через конструктор. Koin
 * сам создаст этот ViewModel и подставит зависимости — никаких аннотаций
 * не требуется.
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: DictionaryRepository,
) : ViewModel() {

    // === State ===
    // _uiState — приватный mutable, можем менять только внутри ViewModel.
    // uiState — публичный immutable, View может только читать.
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // === Поток поисковых запросов ===
    // При каждом изменении строки поиска значение сюда отправляется.
    // На него навешан debounce (см. ниже).
    private val _queryFlow = MutableStateFlow("")

    // Активная корутина поиска. Если приходит новый запрос — отменяем старый,
    // чтобы устаревшие результаты не перезаписали свежие (race condition).
    private var searchJob: Job? = null

    init {
        // Настраиваем pipeline обработки поисковых запросов:
        //   1. debounce(300) — ждём 300 мс после последнего изменения
        //      (пока пользователь печатает, мы НЕ дёргаем сервер на каждый
        //      символ — экономим трафик и батарею).
        //   2. distinctUntilChanged — игнорируем повторяющиеся одинаковые
        //      значения (например, после очистки поля и ввода того же слова).
        //   3. filter — пустые запросы пропускаем, не дёргая API.
        _queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    // Если пользователь стёр всё, очищаем результаты
                    _uiState.update { it.copy(items = emptyList(), total = 0, error = null) }
                } else {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Вызывается View при изменении строки поиска.
     */
    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        _queryFlow.value = newQuery
    }

    /**
     * Очистка поискового поля.
     */
    fun onClearQuery() {
        onQueryChange("")
    }

    /**
     * Сам поиск.
     *
     * viewModelScope — это специальный CoroutineScope, привязанный к жизненному
     * циклу ViewModel. Если ViewModel будет уничтожен (например, пользователь
     * закрыл экран), все запущенные в этом scope корутины автоматически
     * отменятся. Это предотвращает утечки памяти и лишний сетевой трафик.
     */
    private fun performSearch(query: String) {
        // Отменяем предыдущий запрос, если был
        searchJob?.cancel()

        _uiState.update { it.copy(isLoading = true, error = null) }

        searchJob = viewModelScope.launch {
            repository.searchWords(query = query, limit = 20, offset = 0)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.items,
                            total = result.total,
                            elapsedMs = result.elapsedMs,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = emptyList(),
                            error = throwable.toUserMessage(),
                        )
                    }
                }
        }
    }

    /**
     * Преобразование технического исключения в понятное пользователю сообщение.
     * Без этого пользователь увидит "java.net.ConnectException: Failed to connect to /10.0.2.2:8000"
     * вместо "Не удалось подключиться к серверу".
     */
    private fun Throwable.toUserMessage(): String {
        val msg = message.orEmpty().lowercase()
        return when {
            "timeout" in msg -> "Сервер не отвечает. Попробуйте позже."
            "connect" in msg || "unable to resolve" in msg ->
                "Не удалось подключиться к серверу. Проверьте, что backend запущен."
            "404" in msg -> "Запрос не найден на сервере."
            "500" in msg -> "Ошибка на сервере. Попробуйте позже."
            else -> "Произошла ошибка: ${message ?: "неизвестно"}"
        }
    }
}

/**
 * Состояние экрана поиска.
 *
 * Это data class — иммутабельный снапшот состояния UI в каждый момент времени.
 * При любом изменении мы создаём НОВЫЙ объект через .copy() — это запускает
 * recomposition в Compose.
 *
 * Один объект описывает весь экран: что в строке поиска, какие результаты,
 * грузим ли мы что-то, есть ли ошибка. Compose автоматически отрисует
 * соответствующее представление.
 */
data class SearchUiState(
    val query: String = "",
    val items: List<WordSummary> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val elapsedMs: Float = 0f,
)