package com.diploma.chinesedictionary.domain.model

/**
 * Доменные модели для системы списков слов.
 */

/** Системный шаблон в каталоге. */
data class WordListTemplate(
    val id: Int,
    val slug: String,
    val icon: String?,
    val title: String,
    val description: String?,
    val category: ListCategory,
    val wordCount: Int,
)

/**
 * Категория системного списка. Используем enum чтобы:
 *   - Иметь типобезопасный switch (when) на стороне UI
 *   - Связать с локализованным именем для отображения
 *   - При появлении новой категории на бэкенде fallback на UNKNOWN
 */
enum class ListCategory(val code: String, val displayName: String) {
    TRAVEL("travel", "Путешествия"),
    BUSINESS("business", "Бизнес"),
    CULTURE("culture", "Культура"),
    STUDY("study", "Учёба"),
    DAILY("daily", "Повседневное"),
    UNKNOWN("unknown", "Другое");

    companion object {
        fun fromCode(code: String): ListCategory =
            entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** Пользовательский список — краткая информация для экрана "Мои списки". */
data class UserList(
    val id: Int,
    val title: String,
    val icon: String?,
    val wordCount: Int,
    val sourceTemplateId: Int?,
    val sourceTemplateSlug: String?,
    val createdAt: String,
) {
    /** true если список создан из системного шаблона. */
    val isFromTemplate: Boolean get() = sourceTemplateId != null
}

/** Одно слово в пользовательском списке. */
data class UserListItem(
    val wordId: Int,
    val position: Int,
    val headword: String,
    val pinyin: String?,
    val firstTranslation: String?,
)

/** Полные данные пользовательского списка с содержимым. */
data class UserListDetail(
    val id: Int,
    val title: String,
    val icon: String?,
    val sourceTemplateId: Int?,
    val createdAt: String,
    val items: List<UserListItem>,
)
