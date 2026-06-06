package com.diploma.chinesedictionary.domain.model

/**
 * Доменные модели входного HSK-теста.
 */

/**
 * Тип вопроса в тесте.
 * Используем enum для типобезопасной обработки в when {} на UI.
 */
enum class TestQuestionType(val code: String) {
    /** Показан иероглиф + пиньинь, выбрать перевод. */
    CHOOSE_TRANSLATION("choose_translation"),

    /** Показан перевод, выбрать иероглиф. */
    CHOOSE_HANZI("choose_hanzi"),

    /** Показан иероглиф + пиньинь, ввести перевод текстом. */
    TYPE_TRANSLATION("type_translation");

    companion object {
        fun fromCode(code: String): TestQuestionType =
            entries.firstOrNull { it.code == code } ?: CHOOSE_TRANSLATION
    }
}

/** Один вопрос теста. */
data class TestQuestion(
    val type: TestQuestionType,
    val wordId: Int,
    val hanzi: String,
    val pinyin: String?,
    val correctAnswer: String,
    val choices: List<String>,
    val acceptedAnswers: List<String>,
) {
    /**
     * Проверяет, правильный ли ответ пользователя.
     *
     *   - CHOOSE_TRANSLATION: выбран перевод → сравниваем с correctAnswer
     *   - CHOOSE_HANZI: выбран иероглиф → сравниваем с полем hanzi
     *     (correctAnswer для этого типа содержит перевод — он показан как промт)
     *   - TYPE_TRANSLATION: введён текст → проверяем по нормализованному
     *     совпадению с любым из acceptedAnswers
     */
    fun isCorrect(userAnswer: String): Boolean {
        if (userAnswer.isBlank()) return false
        return when (type) {
            TestQuestionType.CHOOSE_TRANSLATION -> userAnswer == correctAnswer
            TestQuestionType.CHOOSE_HANZI -> userAnswer == hanzi
            TestQuestionType.TYPE_TRANSLATION -> {
                val normalized = userAnswer.normalize()
                acceptedAnswers.any { it.normalize() == normalized }
            }
        }
    }

    private fun String.normalize(): String =
        lowercase()
            .replace("ё", "е")
            .trim()
            .trimEnd('.', '!', '?')
            .replace(Regex("\\s+"), " ")
}

/** Пакет вопросов одного HSK-уровня. */
data class TestQuestionBatch(
    val hskLevel: Int,
    val questions: List<TestQuestion>,
)

/** Результат сохранения теста на сервере. */
data class LevelTestSaveResult(
    val determinedHskLevel: Int,
    val appLevel: Int,
    val saved: Boolean,
)