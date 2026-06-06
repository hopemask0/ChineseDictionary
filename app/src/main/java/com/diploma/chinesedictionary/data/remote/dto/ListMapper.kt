package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.ListCategory
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.model.UserListDetail
import com.diploma.chinesedictionary.domain.model.UserListItem
import com.diploma.chinesedictionary.domain.model.WordListTemplate

fun WordListTemplateDto.toDomain(): WordListTemplate = WordListTemplate(
    id = id,
    slug = slug,
    icon = icon,
    title = title,
    description = description,
    category = ListCategory.fromCode(category),
    wordCount = wordCount,
)

fun UserListDto.toDomain(): UserList = UserList(
    id = id,
    title = title,
    icon = icon,
    wordCount = wordCount,
    sourceTemplateId = sourceTemplateId,
    sourceTemplateSlug = sourceTemplateSlug,
    createdAt = createdAt,
)

fun UserListItemDto.toDomain(): UserListItem = UserListItem(
    wordId = wordId,
    position = position,
    headword = headword,
    pinyin = pinyin,
    firstTranslation = firstTranslation,
)

fun UserListDetailDto.toDomain(): UserListDetail = UserListDetail(
    id = id,
    title = title,
    icon = icon,
    sourceTemplateId = sourceTemplateId,
    createdAt = createdAt,
    items = items.map { it.toDomain() },
)
