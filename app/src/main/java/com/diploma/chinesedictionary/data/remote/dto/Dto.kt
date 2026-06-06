package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO (Data Transfer Object) — структуры данных, повторяющие JSON-ответы
 * от нашего FastAPI-бэкенда.
 *
 * Все классы помечены @Serializable — это аннотация Kotlinx Serialization,
 * благодаря которой Kotlin сам умеет преобразовывать JSON в эти объекты
 * и обратно. Никакого ручного парсинга.
 *
 * Аннотация @SerialName("foo") нужна, когда поле в JSON называется иначе,
 * чем поле в Kotlin. По соглашению Python использует snake_case (short_translation),
 * а Kotlin — camelCase (shortTranslation), поэтому маппинг нужен.
 */

@Serializable
data class SearchResponseDto(
    val query: String,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val items: List<SearchResultItemDto>,
    @SerialName("elapsed_ms") val elapsedMs: Float,
)

@Serializable
data class SearchResultItemDto(
    val id: Int,
    val headword: String,
    val pinyin: String? = null,
    @SerialName("short_translation") val shortTranslation: String,
    val category: String,
    @SerialName("hsk_level") val hskLevel: Int? = null,
)

@Serializable
data class WordDetailDto(
    val id: Int,
    val headword: String,
    val pinyin: String? = null,
    @SerialName("pinyin_plain") val pinyinPlain: String? = null,
    val category: String,
    @SerialName("hsk_level") val hskLevel: Int? = null,
    @SerialName("char_length") val charLength: Int,
    val meanings: List<MeaningDto> = emptyList(),
    val labels: List<LabelDto> = emptyList(),
    val references: List<ReferenceDto> = emptyList(),
)

@Serializable
data class MeaningDto(
    val position: Int,
    val level: Int,
    val translation: String,
    val examples: List<ExampleDto> = emptyList(),
)

@Serializable
data class ExampleDto(
    val chinese: String,
    val russian: String? = null,
)

@Serializable
data class LabelDto(
    val name: String,
)

@Serializable
data class ReferenceDto(
    @SerialName("to_headword") val toHeadword: String,
)
