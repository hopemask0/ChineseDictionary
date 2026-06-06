package com.diploma.chinesedictionary.di

import com.diploma.chinesedictionary.BuildConfig
import com.diploma.chinesedictionary.data.remote.AuthInterceptor
import com.diploma.chinesedictionary.data.remote.api.AuthApi
import com.diploma.chinesedictionary.data.remote.api.DictionaryApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Koin-модуль для сетевого стека приложения.
 *
 * Новое в этой версии:
 *   - AuthInterceptor для автоматической подстановки X-User-Id
 *   - AuthApi — Retrofit-интерфейс для эндпоинтов авторизации
 */
val networkModule = module {

    // ===== Json-парсер =====
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    // ===== Interceptor для логирования HTTP в Logcat =====
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // ===== Interceptor для X-User-Id =====
    // get<UserSession>() — Koin найдёт зарегистрированный UserSession
    // в dataModule. Если зависимости описаны в разных модулях, Koin
    // объединяет их в один граф автоматически.
    single<AuthInterceptor> {
        AuthInterceptor(userSession = get())
    }

    // ===== OkHttp клиент с двумя interceptor'ами =====
    // Порядок важен! AuthInterceptor стоит ПЕРЕД LoggingInterceptor,
    // чтобы в логах был виден заголовок X-User-Id (для отладки).
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // ===== Retrofit =====
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // ===== Retrofit-интерфейсы =====
    single<DictionaryApi> {
        get<Retrofit>().create(DictionaryApi::class.java)
    }

    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }

    single<com.diploma.chinesedictionary.data.remote.api.ListsApi> {
        get<Retrofit>().create(com.diploma.chinesedictionary.data.remote.api.ListsApi::class.java)
    }

    single<com.diploma.chinesedictionary.data.remote.api.ProgressApi> {
        get<Retrofit>().create(com.diploma.chinesedictionary.data.remote.api.ProgressApi::class.java)
    }

    single<com.diploma.chinesedictionary.data.remote.api.LevelTestApi> {
        get<Retrofit>().create(com.diploma.chinesedictionary.data.remote.api.LevelTestApi::class.java)
    }
}