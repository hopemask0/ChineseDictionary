package com.diploma.chinesedictionary.data.repository

import com.diploma.chinesedictionary.data.remote.api.ListsApi
import com.diploma.chinesedictionary.data.remote.dto.AddWordRequestDto
import com.diploma.chinesedictionary.data.remote.dto.CreateListRequestDto
import com.diploma.chinesedictionary.data.remote.dto.SubscribeRequestDto
import com.diploma.chinesedictionary.data.remote.dto.UpdateListRequestDto
import com.diploma.chinesedictionary.data.remote.dto.toDomain
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.model.UserListDetail
import com.diploma.chinesedictionary.domain.model.WordListTemplate
import com.diploma.chinesedictionary.domain.repository.ListsRepository

/**
 * Реализация ListsRepository через ListsApi.
 *
 * Каждый метод обёрнут в runCatching — если сеть упадёт или сервер
 * вернёт ошибку, мы получим Result.failure с типизированным исключением,
 * которое ViewModel сможет распознать.
 */
class ListsRepositoryImpl(
    private val api: ListsApi,
) : ListsRepository {

    override suspend fun getCatalog(): Result<List<WordListTemplate>> = runCatching {
        api.getCatalog().map { it.toDomain() }
    }

    override suspend fun getMyLists(): Result<List<UserList>> = runCatching {
        api.getMyLists().map { it.toDomain() }
    }

    override suspend fun createList(title: String, icon: String?): Result<UserList> =
        runCatching {
            api.createList(CreateListRequestDto(title = title.trim(), icon = icon))
                .toDomain()
        }

    override suspend fun subscribeToTemplate(
        templateId: Int,
        customTitle: String?,
    ): Result<UserList> = runCatching {
        api.subscribeToTemplate(
            SubscribeRequestDto(templateId = templateId, customTitle = customTitle?.trim())
        ).toDomain()
    }

    override suspend fun getListDetail(listId: Int): Result<UserListDetail> = runCatching {
        api.getListDetail(listId).toDomain()
    }

    override suspend fun updateList(
        listId: Int, title: String?, icon: String?,
    ): Result<Unit> = runCatching {
        api.updateList(listId, UpdateListRequestDto(title = title?.trim(), icon = icon))
        Unit
    }

    override suspend fun deleteList(listId: Int): Result<Unit> = runCatching {
        api.deleteList(listId)
    }

    override suspend fun addWordToList(listId: Int, wordId: Int): Result<Unit> = runCatching {
        api.addWord(listId, AddWordRequestDto(wordId = wordId))
    }

    override suspend fun removeWordFromList(listId: Int, wordId: Int): Result<Unit> = runCatching {
        api.removeWord(listId, wordId)
    }
}
