package com.diploma.chinesedictionary.data.repository

import com.diploma.chinesedictionary.data.local.UserSession
import com.diploma.chinesedictionary.data.remote.api.AuthApi
import com.diploma.chinesedictionary.data.remote.dto.LoginRequestDto
import com.diploma.chinesedictionary.data.remote.dto.RegisterRequestDto
import com.diploma.chinesedictionary.data.remote.dto.toDomain
import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.User
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import retrofit2.HttpException

/**
 * Реализация AuthRepository.
 *
 * Что тут происходит:
 *   1. Принимаем доменные параметры (Goal, etc).
 *   2. Конвертируем их в DTO для отправки на сервер.
 *   3. Делаем HTTP-запрос через Retrofit.
 *   4. Получаем DTO в ответ → конвертируем в доменную модель.
 *   5. Сохраняем user_id локально в UserSession.
 *   6. Возвращаем Result<User>.
 *
 * runCatching — Kotlin-конструкция, которая выполняет блок и оборачивает
 * результат в Result. Если внутри случится исключение, оно станет
 * Result.failure(exception). Это удобный паттерн для "никогда не падать
 * из-за сетевых проблем".
 */
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val userSession: UserSession,
) : AuthRepository {

    override suspend fun register(
        username: String,
        displayName: String,
        level: Int?,
        goals: List<Goal>,
    ): Result<User> = runCatching {
        // 1. Готовим запрос
        val request = RegisterRequestDto(
            username = username.trim(),
            displayName = displayName.trim(),
            level = level,
            goals = goals.map { it.code },  // enum → строки кодов
        )

        // 2. Шлём — Retrofit сам сериализует и десериализует JSON
        val response = api.register(request)

        // 3. Сохраняем сессию ДО возврата — клиент сразу будет авторизован
        userSession.saveSession(
            userId = response.userId,
            username = response.user.username,
        )

        // 4. Возвращаем доменную модель
        response.user.toDomain()
    }

    override suspend fun login(username: String): Result<User> = runCatching {
        val response = api.login(LoginRequestDto(username = username.trim()))
        userSession.saveSession(
            userId = response.userId,
            username = response.user.username,
        )
        response.user.toDomain()
    }

    override suspend fun getCurrentUser(): Result<User> = runCatching {
        // /auth/me требует X-User-Id — он автоматически добавится через
        // AuthInterceptor, если пользователь залогинен. Если не залогинен,
        // сервер вернёт 401 и Retrofit бросит HttpException.
        api.getMe().toDomain()
    }.onFailure { error ->
        // Если сервер сказал, что наш user_id невалиден (например, БД на
        // сервере была пересоздана), очищаем локальную сессию.
        // 401 = "не передан id", 404 = "id не найден".
        if (error is HttpException && (error.code() == 401 || error.code() == 404)) {
            userSession.clearSession()
        }
    }

    override fun logout() {
        userSession.clearSession()
    }

    override fun isLoggedIn(): Boolean = userSession.isLoggedIn()
}
