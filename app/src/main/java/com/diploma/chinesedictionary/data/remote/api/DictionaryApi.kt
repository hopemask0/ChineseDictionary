package com.diploma.chinesedictionary.data.remote.api

import com.diploma.chinesedictionary.data.remote.dto.CharacterInfoDto
import com.diploma.chinesedictionary.data.remote.dto.CharacterStrokesDto
import com.diploma.chinesedictionary.data.remote.dto.SearchResponseDto
import com.diploma.chinesedictionary.data.remote.dto.WordDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit-интерфейс для FastAPI-бэкенда.
 *
 * Retrofit генерирует реализацию этого интерфейса автоматически в рантайме.
 * Мы просто декларативно описываем эндпоинты через аннотации:
 *   @GET("path")  — HTTP-метод и URL
 *   @Query("name") — параметр query string
 *   @Path("name") — параметр пути URL (например /words/{id})
 *
 * Все методы — suspend. Retrofit понимает suspend нативно начиная с версии
 * 2.6 и автоматически выполняет запрос в фоновом потоке, возвращая результат
 * на вызывающий поток.
 */
interface DictionaryApi {

    /**
     * Поиск слов.
     *
     * Соответствует FastAPI эндпоинту:
     *   GET /api/search?q=...&limit=20&offset=0&category=common&hsk_level=1
     */
    @GET("api/search")
    suspend fun searchWords(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("category") category: String? = null,
        @Query("hsk_level") hskLevel: Int? = null,
    ): SearchResponseDto

    /**
     * Получить полную карточку слова по ID.
     *
     * Соответствует FastAPI эндпоинту:
     *   GET /api/words/{word_id}
     */
    @GET("api/words/{wordId}")
    suspend fun getWordDetail(
        @Path("wordId") wordId: Int,
    ): WordDetailDto

    /**
     * Получить случайное слово для "Слова дня" на главном экране.
     *
     * Соответствует FastAPI эндпоинту:
     *   GET /api/words/random?min_chars=1&max_chars=3&hsk_level=2
     *
     * Все параметры опциональны, сервер вернёт подходящее слово
     * (короткое, с пиньинь, из общей лексики).
     */
    @GET("api/words/random")
    suspend fun getRandomWord(
        @Query("min_chars") minChars: Int? = null,
        @Query("max_chars") maxChars: Int? = null,
        @Query("hsk_level") hskLevel: Int? = null,
    ): WordDetailDto

    /**
     * Получить этимологию всех иероглифов из составного слова.
     *
     * Например, для слова id=42 (学习) вернёт информацию по 学 и 习.
     * Иероглифы, не покрытые Make Me a Hanzi, будут пропущены сервером.
     *
     * Соответствует FastAPI эндпоинту:
     *   GET /api/words/{word_id}/characters
     */
    @GET("api/words/{wordId}/characters")
    suspend fun getWordCharacters(
        @Path("wordId") wordId: Int,
    ): List<CharacterInfoDto>

    /**
     * Получить SVG-данные для анимации написания одного иероглифа.
     *
     * Данные передаются в WebView с библиотекой hanzi-writer.js,
     * которая рисует анимацию.
     */
    @GET("api/characters/{character}/strokes")
    suspend fun getCharacterStrokes(
        @Path("character") character: String,
    ): CharacterStrokesDto
}