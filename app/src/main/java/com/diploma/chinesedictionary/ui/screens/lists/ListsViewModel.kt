package com.diploma.chinesedictionary.ui.screens.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.model.WordListTemplate
import com.diploma.chinesedictionary.domain.repository.ListsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана "Списки".
 *
 * Содержит две вкладки:
 *   - Мои списки (UserList) — то, что пользователь создал или скопировал
 *   - Каталог (WordListTemplate) — системные шаблоны для подписки
 *
 * При загрузке экрана дёргает оба запроса параллельно через async{}.
 */
class ListsViewModel(
    private val repository: ListsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun selectTab(tab: ListsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() = loadAll()

    fun createList(title: String, icon: String?, onSuccess: (Int) -> Unit) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createList(title, icon)
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(myLists = listOf(created) + it.myLists)
                    }
                    onSuccess(created.id)
                }
        }
    }

    fun subscribeToTemplate(templateId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(subscribingTemplateId = templateId) }
            repository.subscribeToTemplate(templateId)
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(
                            myLists = listOf(created) + it.myLists,
                            subscribingTemplateId = null,
                            lastMessage = "Список «${created.title}» добавлен",
                            selectedTab = ListsTab.MINE,  // переключаем на мои
                        )
                    }
                    onSuccess()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            subscribingTemplateId = null,
                            lastMessage = "Не удалось добавить список",
                        )
                    }
                }
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            repository.deleteList(listId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            myLists = it.myLists.filter { l -> l.id != listId },
                            lastMessage = "Список удалён",
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(lastMessage = null) }
    }

    private fun loadAll() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val myDeferred = async { repository.getMyLists() }
            val catalogDeferred = async { repository.getCatalog() }

            val my = myDeferred.await()
            val catalog = catalogDeferred.await()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    myLists = my.getOrNull() ?: emptyList(),
                    catalog = catalog.getOrNull() ?: emptyList(),
                    error = if (my.isFailure && catalog.isFailure) {
                        "Не удалось загрузить списки"
                    } else null,
                )
            }
        }
    }
}

enum class ListsTab { MINE, CATALOG }

data class ListsUiState(
    val isLoading: Boolean = false,
    val selectedTab: ListsTab = ListsTab.MINE,
    val myLists: List<UserList> = emptyList(),
    val catalog: List<WordListTemplate> = emptyList(),
    val subscribingTemplateId: Int? = null,
    val error: String? = null,
    val lastMessage: String? = null,
)
