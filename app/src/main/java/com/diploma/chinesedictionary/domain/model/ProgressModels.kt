package com.diploma.chinesedictionary.domain.model

/**
 * Доменные модели для системы прогресса изучения.
 */

/** Прогресс по одному слову. */
data class WordProgress(
    val wordId: Int,
    val timesSeen: Int,
    val timesKnown: Int,
    val streak: Int,
    val isLearned: Boolean,
    val lastReviewedAt: String?,
)

/** Карточка в тренировке. */
data class TrainerCard(
    val wordId: Int,
    val headword: String,
    val pinyin: String?,
    val translation: String?,
    val isLearned: Boolean,
    val timesSeen: Int,
)

/** Готовая сессия тренировки. */
data class TrainerSession(
    val listId: Int,
    val listTitle: String,
    val cards: List<TrainerCard>,
)

/** Сводная статистика пользователя. */
data class ProgressStats(
    val totalSeen: Int,
    val totalLearned: Int,
    val totalReviews: Int,
)
