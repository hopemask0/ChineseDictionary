package com.diploma.chinesedictionary.data.remote

import com.diploma.chinesedictionary.data.local.UserSession
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp-перехватчик, который добавляет заголовок X-User-Id во все
 * исходящие запросы, если пользователь залогинен.
 *
 * Как это работает:
 *   1. Когда Retrofit готовится отправить запрос (любой), он сначала
 *      передаёт его всем зарегистрированным interceptor'ам.
 *   2. Каждый interceptor может изменить запрос или сразу вернуть ответ
 *      (например, из кэша).
 *   3. Наш interceptor смотрит, есть ли user_id в UserSession,
 *      и если есть — добавляет заголовок.
 *
 * Преимущество перед добавлением @Header в каждый @POST/@GET в Retrofit:
 *   - Не нужно дублировать код в каждом методе API.
 *   - Если завтра логика подстановки изменится (например, JWT-токен с
 *     обновлением) — правим только здесь.
 *   - Запросы /auth/register и /auth/login будут просто без заголовка
 *     (когда user_id ещё нет в сессии).
 */
class AuthInterceptor(
    private val userSession: UserSession,
) : Interceptor {

    /**
     * Этот метод вызывается OkHttp на каждый запрос.
     * chain — это "цепочка" interceptor'ов и финальная отправка запроса.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Получаем оригинальный запрос
        val originalRequest = chain.request()

        // 2. Если пользователь залогинен — добавляем заголовок
        val userId = userSession.getUserId()
        val modifiedRequest = if (userId != null) {
            originalRequest.newBuilder()
                .addHeader("X-User-Id", userId.toString())
                .build()
        } else {
            // Не залогинен — отправляем как есть
            originalRequest
        }

        // 3. Передаём дальше по цепочке (либо следующему interceptor'у,
        //    либо непосредственно на отправку)
        return chain.proceed(modifiedRequest)
    }
}
