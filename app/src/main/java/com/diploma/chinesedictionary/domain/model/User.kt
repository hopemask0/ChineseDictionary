package com.diploma.chinesedictionary.domain.model

/**
 * Доменная модель пользователя.
 *
 * Используется внутри приложения — в ViewModel'ях, на экранах.
 * НЕ имеет аннотаций сериализации и НЕ привязана к формату сервера.
 *
 * Цель: если завтра API сменит формат (например, "display_name" станет
 * "displayName" или "name"), мы поправим только маппер DTO → User,
 * а весь UI продолжит работать.
 */
data class User(
    val id: Int,
    val username: String,
    val displayName: String,
    val level: Int?,  // 1..4 или null = пройти тест
    val goals: List<Goal>,
    val createdAt: String,
)

/**
 * Цель изучения китайского.
 *
 * Используем enum, чтобы:
 *   1. Получить компайл-тайм проверку — нельзя написать "trvel" с опечаткой,
 *      компилятор не пропустит.
 *   2. Удобно делать when (goal) { ... } с гарантией покрытия всех веток.
 *   3. Привязать к каждой цели её отображаемое название и иконку.
 *
 * code — то, что лежит в БД и шлётся по сети.
 * displayName — что показываем пользователю.
 */
enum class Goal(val code: String, val displayName: String) {
    TRAVEL("travel", "Путешествия"),
    BUSINESS("business", "Бизнес и работа"),
    CULTURE("culture", "Культура, кино, дорамы"),
    STUDY("study", "Учёба, школа, вуз"),
    RELOCATION("relocation", "Переезд, эмиграция"),
    HOBBY("hobby", "Для души, хобби");

    companion object {
        /**
         * Безопасное преобразование строкового кода в enum.
         * Если код неизвестен (например, новый, который сервер добавил, а мы ещё нет)
         * — возвращаем null, а не падаем.
         */
        fun fromCode(code: String): Goal? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Уровень владения китайским.
 *
 * Похожий подход: enum со строковым кодом и отображаемым названием.
 * Дополнительное поле hskRange нужно для отображения "HSK 1-2" вместо просто "2".
 */
enum class Level(val code: Int, val displayName: String, val hskRange: String?) {
    BEGINNER(1, "Новичок", null),
    HSK_1_2(2, "HSK 1-2", "Базовый"),
    HSK_3_4(3, "HSK 3-4", "Средний"),
    HSK_5_6(4, "HSK 5-6", "Продвинутый");

    companion object {
        fun fromCode(code: Int?): Level? = entries.firstOrNull { it.code == code }
    }
}
