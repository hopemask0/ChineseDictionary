package com.diploma.chinesedictionary.data.repository

import com.diploma.chinesedictionary.data.remote.api.LevelTestApi
import com.diploma.chinesedictionary.data.remote.dto.LevelTestResultRequestDto
import com.diploma.chinesedictionary.data.remote.dto.toDomain
import com.diploma.chinesedictionary.domain.model.LevelTestSaveResult
import com.diploma.chinesedictionary.domain.model.TestQuestionBatch
import com.diploma.chinesedictionary.domain.repository.LevelTestRepository

class LevelTestRepositoryImpl(
    private val api: LevelTestApi,
) : LevelTestRepository {

    override suspend fun getQuestions(hskLevel: Int): Result<TestQuestionBatch> =
        runCatching {
            api.getQuestions(hskLevel = hskLevel, count = 10).toDomain()
        }

    override suspend fun saveResult(
        determinedLevel: Int,
        answersPerLevel: Map<Int, Pair<Int, Int>>,
    ): Result<LevelTestSaveResult> = runCatching {
        // Преобразуем Map<Int, Pair<Int,Int>> в Map<String, List<Int>> для API
        val payload = LevelTestResultRequestDto(
            determinedLevel = determinedLevel,
            answersPerLevel = answersPerLevel.mapKeys { it.key.toString() }
                .mapValues { listOf(it.value.first, it.value.second) },
        )
        api.saveResult(payload).toDomain()
    }
}
