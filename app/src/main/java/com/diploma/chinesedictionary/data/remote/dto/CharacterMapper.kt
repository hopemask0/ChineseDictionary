package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.domain.model.CharacterStrokes
import com.diploma.chinesedictionary.domain.model.EtymologyType

/**
 * Конвертация DTO иероглифов в доменные модели.
 */

fun CharacterInfoDto.toDomain(): CharacterInfo = CharacterInfo(
    character = character,
    pinyin = pinyin,
    definition = definition,
    decomposition = decomposition,
    radical = radical,
    etymologyType = EtymologyType.fromCode(etymologyType),
    etymologyHint = etymologyHint,
    etymologySemantic = etymologySemantic,
    etymologyPhonetic = etymologyPhonetic,
    hasStrokes = hasStrokes,
)

fun CharacterStrokesDto.toDomain(): CharacterStrokes = CharacterStrokes(
    character = character,
    strokes = strokes,
    medians = medians,
)
