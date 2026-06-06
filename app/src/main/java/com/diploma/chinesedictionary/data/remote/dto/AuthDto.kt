package com.diploma.chinesedictionary.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для эндпоинтов авторизации.
 *
 * Структура повторяет JSON, который шлёт/принимает наш FastAPI.
 * @Serializable + @SerialName — это аннотации Kotlinx Serialization,
 * благодаря которым Kotlin сам конвертирует объект в JSON и обратно.
 *
 * Имена полей в JSON и в Kotlin различаются:
 *   Сервер (Python): snake_case — display_name, hsk_level, user_id
 *   Клиент (Kotlin): camelCase  — displayName, hskLevel, userId
 *
 * @SerialName("display_name") говорит парсеру: "это поле в JSON
 * называется иначе, чем в Kotlin".
 */

// ===========================================================================
// Запросы (что мы шлём на сервер)
// ===========================================================================

@Serializable
data class RegisterRequestDto(
    val username: String,
    @SerialName("display_name") val displayName: String,
    val level: Int? = null,
    val goals: List<String> = emptyList(),
)

@Serializable
data class LoginRequestDto(
    val username: String,
)

// ===========================================================================
// Ответы (что сервер возвращает нам)
// ===========================================================================

/**
 * Ответ /auth/register и /auth/login.
 * Содержит user_id (для X-User-Id заголовка) + полный профиль.
 */
@Serializable
data class AuthResponseDto(
    @SerialName("user_id") val userId: Int,
    val user: UserDto,
)

/**
 * Профиль пользователя — отдаётся отдельным эндпоинтом /auth/me и
 * как часть AuthResponse.
 */
@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    @SerialName("display_name") val displayName: String,
    val level: Int? = null,
    val goals: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String,
)
