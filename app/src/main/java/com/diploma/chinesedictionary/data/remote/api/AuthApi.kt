package com.diploma.chinesedictionary.data.remote.api

import com.diploma.chinesedictionary.data.remote.dto.AuthResponseDto
import com.diploma.chinesedictionary.data.remote.dto.LoginRequestDto
import com.diploma.chinesedictionary.data.remote.dto.RegisterRequestDto
import com.diploma.chinesedictionary.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit-интерфейс для эндпоинтов авторизации.
 *
 * Аннотации:
 *   @POST("path") — это POST-запрос на такой URL
 *   @GET("path")  — это GET-запрос
 *   @Body         — этот параметр пойдёт в тело запроса как JSON
 *
 * suspend — обязательно для современного Retrofit. Это делает функцию
 * "приостанавливаемой" — она запускает запрос в фоновом потоке и
 * возвращает результат на вызывающий поток, не блокируя UI.
 *
 * Под капотом:
 *   1. Retrofit видит @POST + @Body
 *   2. Сериализует RegisterRequestDto в JSON через Kotlinx Serialization
 *   3. Шлёт HTTP-запрос
 *   4. Получает JSON в ответе
 *   5. Десериализует его в AuthResponseDto
 *   6. Возвращает нам готовый объект
 *
 * Если запрос упадёт (нет интернета, 500 от сервера) — выбросится
 * исключение, которое мы обработаем в Repository через runCatching.
 */
interface AuthApi {

    /**
     * POST /api/auth/register
     */
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto,
    ): AuthResponseDto

    /**
     * POST /api/auth/login
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): AuthResponseDto

    /**
     * GET /api/auth/me
     *
     * X-User-Id передаётся НЕ здесь, а через interceptor — глобально
     * для всех запросов. Так нам не нужно прописывать @Header в каждом
     * методе.
     */
    @GET("api/auth/me")
    suspend fun getMe(): UserDto
}
