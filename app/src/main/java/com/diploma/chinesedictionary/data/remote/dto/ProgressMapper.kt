package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.ProgressStats
import com.diploma.chinesedictionary.domain.model.TrainerCard
import com.diploma.chinesedictionary.domain.model.TrainerSession
import com.diploma.chinesedictionary.domain.model.WordProgress

fun ProgressItemDto.toDomain(): WordProgress = WordProgress(
    wordId = wordId,
    timesSeen = timesSeen,
    timesKnown = timesKnown,
    streak = streak,
    isLearned = isLearned,
    lastReviewedAt = lastReviewedAt,
)

fun TrainerCardDto.toDomain(): TrainerCard = TrainerCard(
    wordId = wordId,
    headword = headword,
    pinyin = pinyin,
    translation = translation,
    isLearned = isLearned,
    timesSeen = timesSeen,
)

fun TrainerSessionDto.toDomain(): TrainerSession = TrainerSession(
    listId = listId,
    listTitle = listTitle,
    cards = cards.map { it.toDomain() },
)

fun ProgressStatsDto.toDomain(): ProgressStats = ProgressStats(
    totalSeen = totalSeen,
    totalLearned = totalLearned,
    totalReviews = totalReviews,
)
