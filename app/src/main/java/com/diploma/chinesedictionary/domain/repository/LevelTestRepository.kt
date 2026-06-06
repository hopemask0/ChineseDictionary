package com.diploma.chinesedictionary.domain.repository

import com.diploma.chinesedictionary.domain.model.LevelTestSaveResult
import com.diploma.chinesedictionary.domain.model.TestQuestionBatch

interface LevelTestRepository {

    /** Получить пакет из 10 вопросов нужного HSK уровня. */
    suspend fun getQuestions(hskLevel: Int): Result<TestQuestionBatch>

    /**
     * Сохранить финальный определённый уровень в профиле.
     *
     * @param determinedLevel — финальный HSK 1..6
     * @param answersPerLevel — { "1": [correct, total], "2": [correct, total], ... }
     */
    suspend fun saveResult(
        determinedLevel: Int,
        answersPerLevel: Map<Int, Pair<Int, Int>>,
    ): Result<LevelTestSaveResult>
}
