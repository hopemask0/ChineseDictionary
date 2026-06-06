package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для системы прогресса и тренажёра.
 */

/** Тело запроса для записи ответа пользователя. */
@Serializable
data class ReviewAnswerDto(
    @SerialName("word_id") val wordId: Int,
    val knows: Boolean,
)

/** Прогресс по одному слову — возвращается после записи ответа. */
@Serializable
data class ProgressItemDto(
    @SerialName("word_id") val wordId: Int,
    @SerialName("times_seen") val timesSeen: Int,
    @SerialName("times_known") val timesKnown: Int,
    val streak: Int,
    @SerialName("is_learned") val isLearned: Boolean,
    @SerialName("last_reviewed_at") val lastReviewedAt: String? = null,
)

/** Одна карточка в режиме тренировки. */
@Serializable
data class TrainerCardDto(
    @SerialName("word_id") val wordId: Int,
    val headword: String,
    val pinyin: String? = null,
    val translation: String? = null,
    @SerialName("is_learned") val isLearned: Boolean,
    @SerialName("times_seen") val timesSeen: Int,
)

/** Готовая сессия тренировки — список + все карточки. */
@Serializable
data class TrainerSessionDto(
    @SerialName("list_id") val listId: Int,
    @SerialName("list_title") val listTitle: String,
    val cards: List<TrainerCardDto>,
)

/** Сводная статистика пользователя. */
@Serializable
data class ProgressStatsDto(
    @SerialName("total_seen") val totalSeen: Int,
    @SerialName("total_learned") val totalLearned: Int,
    @SerialName("total_reviews") val totalReviews: Int,
)
