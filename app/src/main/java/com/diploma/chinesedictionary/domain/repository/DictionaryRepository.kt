package com.diploma.chinesedictionary.domain.repository

import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.domain.model.CharacterStrokes
import com.diploma.chinesedictionary.domain.model.SearchResult
import com.diploma.chinesedictionary.domain.model.WordDetail

/**
 * Интерфейс репозитория словаря.
 *
 * Этот интерфейс — контракт между слоем UI/Domain и слоем Data.
 * UI знает только про интерфейс, а конкретную реализацию (через Retrofit,
 * Room или mock-данные) подставляет Hilt в момент запуска приложения.
 *
 * Это даёт два главных преимущества:
 *   1. Тестируемость: для unit-тестов можно подсунуть фейковую реализацию.
 *   2. Гибкость: в будущей итерации мы добавим офлайн-кэш через Room.
 *      Изменится только реализация репозитория, ViewModel останется тот же.
 *
 * Все методы — suspend, потому что они делают сетевые запросы.
 * suspend — это ключевое слово Kotlin для асинхронных функций (Coroutines).
 * Вызывающий код может ждать результат, не блокируя UI-поток.
 */
interface DictionaryRepository {

    /**
     * Поиск слов по запросу.
     *
     * Возвращает Result — стандартный Kotlin-тип для "успех или ошибка".
     * Использование Result вместо try/catch в каждом ViewModel — это паттерн,
     * упрощающий обработку ошибок: ошибка проброшена как значение, а не
     * исключение.
     */
    suspend fun searchWords(
        query: String,
        limit: Int = 20,
        offset: Int = 0,
        category: String? = null,
        hskLevel: Int? = null,
    ): Result<SearchResult>

    /**
     * Получить полную карточку слова по ID.
     */
    suspend fun getWordDetail(wordId: Int): Result<WordDetail>

    /**
     * Получить случайное слово для "Слова дня" на главном экране.
     *
     * Параметры опциональны — если null, сервер использует значения
     * по умолчанию (короткие слова из общей лексики).
     */
    suspend fun getRandomWord(
        minChars: Int? = null,
        maxChars: Int? = null,
        hskLevel: Int? = null,
    ): Result<WordDetail>

    /**
     * Получить этимологию всех иероглифов из составного слова.
     *
     * Для слова id=42 (например, 学习) вернёт по одной записи на 学 и 习.
     * Иероглифы без данных (вне базы Make Me a Hanzi) пропускаются на сервере.
     *
     * Никогда не выдаёт ошибку "не найдено" — для слова без покрытых
     * иероглифов вернёт пустой список.
     */
    suspend fun getWordCharacters(wordId: Int): Result<List<CharacterInfo>>

    /**
     * Получить данные для анимации написания одного иероглифа.
     * Используется в WebView с hanzi-writer.js.
     */
    suspend fun getCharacterStrokes(character: String): Result<CharacterStrokes>
}