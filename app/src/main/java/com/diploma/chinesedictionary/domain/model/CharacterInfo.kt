package com.diploma.chinesedictionary.domain.model

/**
 * Доменная модель иероглифа: этимология, декомпозиция, радикал.
 * Используется в блоке "Структура" на карточке слова.
 *
 * etymologyType может быть:
 *   - "pictographic"  — пиктограмма (форма произошла от рисунка предмета)
 *   - "ideographic"   — идеограмма (форма передаёт идею)
 *   - "pictophonetic" — фоносемантический (один компонент = смысл, другой = звук)
 *   - null            — нет данных
 */
data class CharacterInfo(
    val character: String,
    val pinyin: List<String>,
    val definition: String?,        // англоязычное определение из MMaH
    val decomposition: String?,     // например "⿰女子" для 好
    val radical: String?,           // ключ иероглифа, например 女 для 好
    val etymologyType: EtymologyType?,
    val etymologyHint: String?,
    val etymologySemantic: String?, // для pictophonetic
    val etymologyPhonetic: String?, // для pictophonetic
    val hasStrokes: Boolean,        // доступна ли анимация
)

/**
 * Тип этимологии иероглифа.
 *
 * Enum даёт нам:
 *   1. Безопасное сопоставление "тип -> отображаемое название на русском"
 *   2. Защиту от опечаток в сравнениях (компилятор не пропустит "pictopgraphic")
 *   3. Лёгкое добавление новых типов в будущем
 */
enum class EtymologyType(val code: String, val displayName: String, val description: String) {
    PICTOGRAPHIC(
        code = "pictographic",
        displayName = "Пиктограмма",
        description = "Форма иероглифа произошла от рисунка предмета",
    ),
    IDEOGRAPHIC(
        code = "ideographic",
        displayName = "Идеограмма",
        description = "Форма передаёт идею через сочетание элементов",
    ),
    PICTOPHONETIC(
        code = "pictophonetic",
        displayName = "Фоносемантический",
        description = "Один компонент передаёт смысл, другой — произношение",
    );

    companion object {
        fun fromCode(code: String?): EtymologyType? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Данные для анимации написания иероглифа.
 * Передаётся в WebView с hanzi-writer.js.
 */
data class CharacterStrokes(
    val character: String,
    val strokes: List<String>,
    val medians: List<List<List<Int>>>,
)
