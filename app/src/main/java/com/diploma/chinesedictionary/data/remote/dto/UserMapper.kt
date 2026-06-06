package com.diploma.chinesedictionary.data.remote.dto

import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.User

/**
 * Конвертация DTO → Domain.
 *
 * Это extension-функция Kotlin: пишется как метод класса, но определена
 * снаружи. Удобно — можно вызывать `userDto.toDomain()` и читать слева
 * направо.
 *
 * mapNotNull — Kotlin'овский метод, который применяет преобразование
 * к каждому элементу списка и отбрасывает null. Если сервер пришлёт
 * неизвестный код цели (например, новый, который мы ещё не знаем),
 * Goal.fromCode вернёт null, и эта цель просто пропадёт из списка —
 * приложение не упадёт, останется работоспособным.
 */
fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    displayName = displayName,
    level = level,
    goals = goals.mapNotNull { Goal.fromCode(it) },
    createdAt = createdAt,
)
