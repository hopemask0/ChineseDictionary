package com.diploma.chinesedictionary.domain.repository

import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.model.UserListDetail
import com.diploma.chinesedictionary.domain.model.WordListTemplate

/**
 * Контракт репозитория списков.
 *
 * Все методы возвращают Result<T> — ошибки (нет сети, 409 конфликт,
 * 404 не найден) оборачиваются в Result.failure, и ViewModel получает
 * чистый результат без необходимости ловить исключения.
 */
interface ListsRepository {

    /** Получить каталог системных шаблонов. */
    suspend fun getCatalog(): Result<List<WordListTemplate>>

    /** Получить все мои списки. */
    suspend fun getMyLists(): Result<List<UserList>>

    /** Создать новый пустой список. */
    suspend fun createList(title: String, icon: String?): Result<UserList>

    /** Подписаться на системный шаблон (= создать копию у себя). */
    suspend fun subscribeToTemplate(
        templateId: Int,
        customTitle: String? = null,
    ): Result<UserList>

    /** Получить полные данные списка со словами. */
    suspend fun getListDetail(listId: Int): Result<UserListDetail>

    /** Переименовать список или сменить иконку. */
    suspend fun updateList(
        listId: Int,
        title: String? = null,
        icon: String? = null,
    ): Result<Unit>

    /** Удалить список. */
    suspend fun deleteList(listId: Int): Result<Unit>

    /** Добавить слово в список. */
    suspend fun addWordToList(listId: Int, wordId: Int): Result<Unit>

    /** Удалить слово из списка. */
    suspend fun removeWordFromList(listId: Int, wordId: Int): Result<Unit>
}
