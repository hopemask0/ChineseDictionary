package com.diploma.chinesedictionary.ui.screens.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.UserListDetail
import com.diploma.chinesedictionary.domain.repository.ListsRepository
import com.diploma.chinesedictionary.ui.navigation.NavArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана конкретного списка с его словами.
 * Поддерживает удаление слов из списка.
 */
class ListDetailViewModel(
    private val repository: ListsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val listId: Int = savedStateHandle.get<Int>(NavArgs.LIST_ID)
        ?: error("ListDetailViewModel requires listId argument")

    private val _uiState = MutableStateFlow(ListDetailUiState(isLoading = true))
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    /**
     * Удалить слово из списка.
     * Оптимистично убираем из UI сразу, если сервер потом откажет — вернём.
     */
    fun removeWord(wordId: Int) {
        val currentItems = _uiState.value.detail?.items ?: return
        val removedItem = currentItems.firstOrNull { it.wordId == wordId } ?: return

        // Оптимистичное обновление
        _uiState.update { state ->
            val detail = state.detail ?: return@update state
            state.copy(detail = detail.copy(items = detail.items - removedItem))
        }

        viewModelScope.launch {
            repository.removeWordFromList(listId, wordId)
                .onFailure {
                    // Откатываем — добавляем обратно
                    _uiState.update { state ->
                        val detail = state.detail ?: return@update state
                        state.copy(
                            detail = detail.copy(items = detail.items + removedItem),
                            lastMessage = "Не удалось удалить слово",
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(lastMessage = null) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getListDetail(listId)
                .onSuccess { detail ->
                    _uiState.update { it.copy(isLoading = false, detail = detail) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось загрузить список",
                        )
                    }
                }
        }
    }
}

data class ListDetailUiState(
    val isLoading: Boolean = false,
    val detail: UserListDetail? = null,
    val error: String? = null,
    val lastMessage: String? = null,
)
