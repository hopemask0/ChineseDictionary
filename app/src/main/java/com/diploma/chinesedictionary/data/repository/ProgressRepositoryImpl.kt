package com.diploma.chinesedictionary.data.repository

import com.diploma.chinesedictionary.data.remote.api.ProgressApi
import com.diploma.chinesedictionary.data.remote.dto.ReviewAnswerDto
import com.diploma.chinesedictionary.data.remote.dto.toDomain
import com.diploma.chinesedictionary.domain.model.ProgressStats
import com.diploma.chinesedictionary.domain.model.TrainerSession
import com.diploma.chinesedictionary.domain.model.WordProgress
import com.diploma.chinesedictionary.domain.repository.ProgressRepository

class ProgressRepositoryImpl(
    private val api: ProgressApi,
) : ProgressRepository {

    override suspend fun getSession(listId: Int): Result<TrainerSession> = runCatching {
        api.getSession(listId).toDomain()
    }

    override suspend fun recordAnswer(wordId: Int, knows: Boolean): Result<WordProgress> =
        runCatching {
            api.review(ReviewAnswerDto(wordId = wordId, knows = knows)).toDomain()
        }

    override suspend fun getStats(): Result<ProgressStats> = runCatching {
        api.getStats().toDomain()
    }
}
