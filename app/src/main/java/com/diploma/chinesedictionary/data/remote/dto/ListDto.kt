package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для системы списков слов.
 * Структуры точно повторяют JSON, отдаваемый FastAPI.
 */

// ===== Системный шаблон в каталоге =====

@Serializable
data class WordListTemplateDto(
    val id: Int,
    val slug: String,
    val icon: String? = null,
    val title: String,
    val description: String? = null,
    val category: String,
    @SerialName("word_count") val wordCount: Int,
)

// ===== Пользовательский список (краткая инфо для каталога "Мои списки") =====

@Serializable
data class UserListDto(
    val id: Int,
    val title: String,
    val icon: String? = null,
    @SerialName("word_count") val wordCount: Int,
    @SerialName("source_template_id") val sourceTemplateId: Int? = null,
    @SerialName("source_template_slug") val sourceTemplateSlug: String? = null,
    @SerialName("created_at") val createdAt: String,
)

// ===== Содержимое списка (детальный экран) =====

@Serializable
data class UserListItemDto(
    @SerialName("word_id") val wordId: Int,
    val position: Int,
    val headword: String,
    val pinyin: String? = null,
    @SerialName("first_translation") val firstTranslation: String? = null,
)

@Serializable
data class UserListDetailDto(
    val id: Int,
    val title: String,
    val icon: String? = null,
    @SerialName("source_template_id") val sourceTemplateId: Int? = null,
    @SerialName("created_at") val createdAt: String,
    val items: List<UserListItemDto>,
)

// ===== Тела запросов =====

@Serializable
data class CreateListRequestDto(
    val title: String,
    val icon: String? = null,
)

@Serializable
data class SubscribeRequestDto(
    @SerialName("template_id") val templateId: Int,
    @SerialName("custom_title") val customTitle: String? = null,
)

@Serializable
data class UpdateListRequestDto(
    val title: String? = null,
    val icon: String? = null,
)

@Serializable
data class AddWordRequestDto(
    @SerialName("word_id") val wordId: Int,
)
