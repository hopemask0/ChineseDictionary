package com.diploma.chinesedictionary.data.repository

import com.diploma.chinesedictionary.data.remote.api.DictionaryApi
import com.diploma.chinesedictionary.data.remote.dto.toDomain
import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.domain.model.CharacterStrokes
import com.diploma.chinesedictionary.domain.model.SearchResult
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository

/**
 * Реализация интерфейса DictionaryRepository через сетевой API.
 *
 * Зависимость от DictionaryApi передаётся через конструктор. Создание этого
 * класса и подстановку зависимости делает Koin (см. dataModule в пакете di/).
 *
 * runCatching — стандартная Kotlin-функция, которая выполняет блок и
 * оборачивает результат в Result.success(...) или Result.failure(exception).
 *
 * Это идиоматичный способ обработки ошибок в Kotlin вместо try/catch.
 * Сетевые исключения (нет интернета, таймаут, 500-я ошибка сервера) будут
 * обёрнуты в Result.failure, и ViewModel сможет показать пользователю
 * понятную ошибку.
 */
class DictionaryRepositoryImpl(
    private val api: DictionaryApi,
) : DictionaryRepository {

    override suspend fun searchWords(
        query: String,
        limit: Int,
        offset: Int,
        category: String?,
        hskLevel: Int?,
    ): Result<SearchResult> = runCatching {
        api.searchWords(
            query = query,
            limit = limit,
            offset = offset,
            category = category,
            hskLevel = hskLevel,
        ).toDomain()
    }

    override suspend fun getWordDetail(wordId: Int): Result<WordDetail> =
        runCatching { api.getWordDetail(wordId).toDomain() }

    override suspend fun getRandomWord(
        minChars: Int?,
        maxChars: Int?,
        hskLevel: Int?,
    ): Result<WordDetail> = runCatching {
        api.getRandomWord(
            minChars = minChars,
            maxChars = maxChars,
            hskLevel = hskLevel,
        ).toDomain()
    }

    override suspend fun getWordCharacters(wordId: Int): Result<List<CharacterInfo>> =
        runCatching {
            api.getWordCharacters(wordId).map { it.toDomain() }
        }

    override suspend fun getCharacterStrokes(character: String): Result<CharacterStrokes> =
        runCatching {
            api.getCharacterStrokes(character).toDomain()
        }
}