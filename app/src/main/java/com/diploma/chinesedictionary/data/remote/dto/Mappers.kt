package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.Example
import com.diploma.chinesedictionary.domain.model.Meaning
import com.diploma.chinesedictionary.domain.model.SearchResult
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.domain.model.WordSummary

/**
 * Мапперы из DTO в Domain-модели.
 *
 * Это extension-функции Kotlin (написаны как ".toX()"). Они конвертируют
 * "сетевые" объекты (DTO) в "приложенческие" (Domain).
 *
 * Зачем разделять? Если завтра поля в JSON переименуются, мы поправим только
 * эти мапперы, а весь UI продолжит работать. Также удобно: иногда нужно
 * объединить несколько DTO в одну Domain-модель или, наоборот, разбить.
 */

fun SearchResultItemDto.toDomain(): WordSummary = WordSummary(
    id = id,
    headword = headword,
    pinyin = pinyin,
    shortTranslation = shortTranslation,
    category = category,
    hskLevel = hskLevel,
)

fun SearchResponseDto.toDomain(): SearchResult = SearchResult(
    query = query,
    total = total,
    items = items.map { it.toDomain() },
    limit = limit,
    offset = offset,
    elapsedMs = elapsedMs,
)

fun ExampleDto.toDomain(): Example = Example(
    chinese = chinese,
    russian = russian,
)

fun MeaningDto.toDomain(): Meaning = Meaning(
    position = position,
    level = level,
    translation = translation,
    examples = examples.map { it.toDomain() },
)

fun WordDetailDto.toDomain(): WordDetail = WordDetail(
    id = id,
    headword = headword,
    pinyin = pinyin,
    pinyinPlain = pinyinPlain,
    category = category,
    hskLevel = hskLevel,
    charLength = charLength,
    meanings = meanings.map { it.toDomain() },
    labels = labels.map { it.name },
    references = references.map { it.toHeadword },
)
