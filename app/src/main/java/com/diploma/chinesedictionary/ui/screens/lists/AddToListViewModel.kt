package com.diploma.chinesedictionary.ui.screens.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.repository.ListsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для bottom-sheet "Добавить слово в список".
 *
 * Используется из карточки слова: пользователь нажимает "+ в список",
 * открывается шторка со всеми его списками + кнопка "Создать новый".
 *
 * Не зависит от конкретного wordId — он передаётся в момент клика.
 * Это позволяет один ViewModel переиспользовать для разных слов.
 */
class AddToListViewModel(
    private val repository: ListsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddToListUiState(isLoading = true))
    val uiState: StateFlow<AddToListUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    fun refresh() = loadLists()

    /**
     * Добавить указанное слово в указанный список.
     * После успеха показываем сообщение и обновляем wordCount списка.
     */
    fun addWordToList(listId: Int, wordId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(addingToListId = listId) }
            repository.addWordToList(listId, wordId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            addingToListId = null,
                            lists = state.lists.map { l ->
                                if (l.id == listId) l.copy(wordCount = l.wordCount + 1) else l
                            },
                            lastMessage = "Слово добавлено",
                        )
                    }
                    onSuccess()
                }
                .onFailure { err ->
                    val msg = if (err.message?.contains("409") == true ||
                                  err.message?.contains("already") == true) {
                        "Слово уже есть в этом списке"
                    } else {
                        "Не удалось добавить слово"
                    }
                    _uiState.update {
                        it.copy(addingToListId = null, lastMessage = msg)
                    }
                }
        }
    }

    /**
     * Создать новый список и сразу добавить туда слово.
     */
    fun createListAndAdd(title: String, wordId: Int, onSuccess: () -> Unit) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createList(title.trim(), icon = null)
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(lists = listOf(created) + it.lists)
                    }
                    // И сразу добавляем туда слово
                    addWordToList(created.id, wordId, onSuccess)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(lastMessage = "Не удалось создать список")
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(lastMessage = null) }
    }

    private fun loadLists() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getMyLists()
                .onSuccess { lists ->
                    _uiState.update { it.copy(isLoading = false, lists = lists) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, lastMessage = "Не удалось загрузить списки")
                    }
                }
        }
    }
}

data class AddToListUiState(
    val isLoading: Boolean = false,
    val lists: List<UserList> = emptyList(),
    val addingToListId: Int? = null,
    val lastMessage: String? = null,
)
