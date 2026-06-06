package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для эндпоинтов работы с иероглифами (этимология, штрихи).
 *
 * Источник данных: Make Me a Hanzi (~9000 иероглифов).
 */

/**
 * GET /api/characters/{ch} — информация для блока "Структура" на карточке слова.
 */
@Serializable
data class CharacterInfoDto(
    val character: String,
    val pinyin: List<String> = emptyList(),
    val definition: String? = null,
    val decomposition: String? = null,
    val radical: String? = null,
    @SerialName("etymology_type") val etymologyType: String? = null,
    @SerialName("etymology_hint") val etymologyHint: String? = null,
    @SerialName("etymology_semantic") val etymologySemantic: String? = null,
    @SerialName("etymology_phonetic") val etymologyPhonetic: String? = null,
    @SerialName("has_strokes") val hasStrokes: Boolean = false,
)

/**
 * GET /api/characters/{ch}/strokes — данные для анимации написания.
 *
 * Это передаётся в WebView с hanzi-writer.js, который рисует анимацию.
 *
 * strokes — массив SVG path-строк (каждая черта отдельно)
 * medians — массив массивов точек (центральные линии штрихов для движения кисти)
 *           Самый внутренний тип List<Int> — это [x, y] координаты.
 */
@Serializable
data class CharacterStrokesDto(
    val character: String,
    val strokes: List<String>,
    val medians: List<List<List<Int>>>,
)
