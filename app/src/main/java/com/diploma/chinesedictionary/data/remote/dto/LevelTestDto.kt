package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для входного HSK-теста.
 */

/** Один вопрос — структура зависит от question_type, см. сервис. */
@Serializable
data class TestQuestionDto(
    @SerialName("question_type") val questionType: String,
    @SerialName("word_id") val wordId: Int,
    val hanzi: String,
    val pinyin: String? = null,
    @SerialName("correct_answer") val correctAnswer: String,
    val choices: List<String> = emptyList(),
    @SerialName("accepted_answers") val acceptedAnswers: List<String> = emptyList(),
)

/** Пакет вопросов одного HSK уровня (обычно 10 штук). */
@Serializable
data class TestQuestionBatchDto(
    @SerialName("hsk_level") val hskLevel: Int,
    val questions: List<TestQuestionDto>,
)

/** Тело запроса для сохранения результата теста. */
@Serializable
data class LevelTestResultRequestDto(
    @SerialName("determined_level") val determinedLevel: Int,
    @SerialName("answers_per_level") val answersPerLevel: Map<String, List<Int>>,
)

/** Ответ сервера после сохранения. */
@Serializable
data class LevelTestResultResponseDto(
    @SerialName("determined_hsk_level") val determinedHskLevel: Int,
    @SerialName("app_level") val appLevel: Int,
    val saved: Boolean,
)
