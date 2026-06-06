package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.LevelTestSaveResult
import com.diploma.chinesedictionary.domain.model.TestQuestion
import com.diploma.chinesedictionary.domain.model.TestQuestionBatch
import com.diploma.chinesedictionary.domain.model.TestQuestionType

fun TestQuestionDto.toDomain(): TestQuestion = TestQuestion(
    type = TestQuestionType.fromCode(questionType),
    wordId = wordId,
    hanzi = hanzi,
    pinyin = pinyin,
    correctAnswer = correctAnswer,
    choices = choices,
    acceptedAnswers = acceptedAnswers,
)

fun TestQuestionBatchDto.toDomain(): TestQuestionBatch = TestQuestionBatch(
    hskLevel = hskLevel,
    questions = questions.map { it.toDomain() },
)

fun LevelTestResultResponseDto.toDomain(): LevelTestSaveResult = LevelTestSaveResult(
    determinedHskLevel = determinedHskLevel,
    appLevel = appLevel,
    saved = saved,
)
