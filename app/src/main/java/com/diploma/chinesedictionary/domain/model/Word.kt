package com.diploma.chinesedictionary.domain.model

/**
 * Краткая информация о слове для отображения в списке результатов поиска.
 *
 * Это "доменная модель" — внутренняя модель приложения, в отличие от DTO
 * (Data Transfer Object), которая описывает JSON с сервера. Разделение
 * на два типа моделей кажется избыточным для маленького проекта, но даёт
 * важное преимущество: если завтра сервер изменит формат JSON, мы поправим
 * только маппинг в DataLayer, а UI и остальной код останутся прежними.
 *
 * Используем data class — это специальный класс Kotlin, который автоматически
 * получает equals(), hashCode(), toString(), copy() — всё, что нужно для
 * корректной работы с Compose и сравнения объектов.
 */
data class WordSummary(
    val id: Int,
    val headword: String,        // 学习
    val pinyin: String?,         // xué xí (может быть null)
    val shortTranslation: String, // "учиться, заниматься..."
    val category: String,        // common | term | proper_noun | rare
    val hskLevel: Int?,          // 1-6 или null
)

/**
 * Полная информация о слове — отображается на экране карточки.
 * Сюда входят все значения, примеры, пометы и ссылки на другие слова.
 */
data class WordDetail(
    val id: Int,
    val headword: String,
    val pinyin: String?,
    val pinyinPlain: String?,
    val category: String,
    val hskLevel: Int?,
    val charLength: Int,
    val meanings: List<Meaning>,
    val labels: List<String>,
    val references: List<String>,
)

/**
 * Одно значение слова (у слова может быть несколько значений).
 */
data class Meaning(
    val position: Int,
    val level: Int,
    val translation: String,
    val examples: List<Example>,
)

/**
 * Пример использования слова на китайском + перевод на русский.
 */
data class Example(
    val chinese: String,
    val russian: String?,
)

/**
 * Полный результат поиска: список + метаданные пагинации.
 *
 * Класс sealed (в данном случае открытый — мы не используем sealed,
 * но для расширения в будущем можно сделать sealed hierarchy
 * Success/Error/Empty).
 */
data class SearchResult(
    val query: String,
    val total: Int,
    val items: List<WordSummary>,
    val limit: Int,
    val offset: Int,
    val elapsedMs: Float,
)
