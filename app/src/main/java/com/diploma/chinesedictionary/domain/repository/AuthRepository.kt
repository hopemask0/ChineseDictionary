package com.diploma.chinesedictionary.domain.repository

import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.User

/**
 * Контракт репозитория авторизации.
 *
 * Это ИНТЕРФЕЙС — он описывает ЧТО умеет репозиторий, но не КАК он это
 * делает. Конкретная реализация (с HTTP-запросами через Retrofit)
 * находится в AuthRepositoryImpl в слое data/.
 *
 * UI и ViewModel зависят только от этого интерфейса. Это значит:
 *   - На тестах можно подсунуть фейковую реализацию.
 *   - Завтра, если решим хранить пользователя локально без сервера, —
 *     поменяем только реализацию.
 *
 * Все методы возвращают Result<T>. Это стандартный Kotlin-тип для
 * "успех или ошибка":
 *   - Result.success(value) если всё ок
 *   - Result.failure(exception) если случилась ошибка
 *
 * Использование в ViewModel:
 *   repository.register(...)
 *       .onSuccess { user -> /* успех */ }
 *       .onFailure { error -> /* показать сообщение */ }
 */
interface AuthRepository {

    /**
     * Зарегистрировать нового пользователя.
     * После успеха автоматически сохраняет user_id локально.
     */
    suspend fun register(
        username: String,
        displayName: String,
        level: Int?,
        goals: List<Goal>,
    ): Result<User>

    /**
     * Вход существующего пользователя по username.
     * После успеха автоматически сохраняет user_id локально.
     */
    suspend fun login(username: String): Result<User>

    /**
     * Загрузить профиль текущего пользователя с сервера.
     * Используется при старте приложения для проверки валидности
     * сохранённого user_id.
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * Выход: очищает локальную сессию.
     * Не делает HTTP-запросов (упрощённая схема без серверных сессий).
     */
    fun logout()

    /**
     * Проверка: есть ли локально сохранённый user_id.
     * Синхронный метод, потому что просто читаем SharedPreferences.
     */
    fun isLoggedIn(): Boolean
}
