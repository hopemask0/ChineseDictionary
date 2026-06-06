package com.diploma.chinesedictionary.ui.screens.worddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository
import com.diploma.chinesedictionary.ui.navigation.NavArgs
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана карточки слова.
 *
 * Загружает данные параллельно:
 *   1. Полная карточка слова (значения, примеры, пометы)
 *   2. Этимология каждого иероглифа слова (новое в Итерации 4.4)
 *
 * Параллельная загрузка через async {} даёт лучшее UX: пока приходит
 * основная карточка, этимология уже тоже идёт. Если этимология опаздывает —
 * не блокирует показ карточки.
 *
 * Этимология может вернуть пустой список — это нормально для слов, чьих
 * иероглифов нет в базе Make Me a Hanzi (~9000 иероглифов покрытия).
 */
class WordDetailViewModel(
    private val repository: DictionaryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val wordId: Int = savedStateHandle.get<Int>(NavArgs.WORD_ID)
        ?: error("WordDetailViewModel requires wordId argument")

    private val _uiState = MutableStateFlow(WordDetailUiState(isLoading = true))
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun retry() {
        loadAll()
    }

    private fun loadAll() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Запускаем оба запроса параллельно
            val wordDeferred = async { repository.getWordDetail(wordId) }
            val charactersDeferred = async { repository.getWordCharacters(wordId) }

            val wordResult = wordDeferred.await()
            val charactersResult = charactersDeferred.await()

            wordResult
                .onSuccess { word ->
                    // Этимология опциональна: даже если её получение упало,
                    // основную карточку показываем
                    val characters = charactersResult.getOrNull() ?: emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            word = word,
                            characters = characters,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            word = null,
                            characters = emptyList(),
                            error = throwable.message ?: "Не удалось загрузить слово",
                        )
                    }
                }
        }
    }
}

data class WordDetailUiState(
    val isLoading: Boolean = false,
    val word: WordDetail? = null,
    val characters: List<CharacterInfo> = emptyList(),  // этимология иероглифов слова
    val error: String? = null,
)