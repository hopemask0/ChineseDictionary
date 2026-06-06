package com.diploma.chinesedictionary.domain.repository

import com.diploma.chinesedictionary.domain.model.ProgressStats
import com.diploma.chinesedictionary.domain.model.TrainerSession
import com.diploma.chinesedictionary.domain.model.WordProgress

interface ProgressRepository {
    suspend fun getSession(listId: Int): Result<TrainerSession>
    suspend fun recordAnswer(wordId: Int, knows: Boolean): Result<WordProgress>
    suspend fun getStats(): Result<ProgressStats>
}
