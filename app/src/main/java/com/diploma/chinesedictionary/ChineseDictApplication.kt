package com.diploma.chinesedictionary

import android.app.Application
import com.diploma.chinesedictionary.di.dataModule
import com.diploma.chinesedictionary.di.networkModule
import com.diploma.chinesedictionary.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Главный класс приложения.
 *
 * Здесь инициализируется Koin — наш Dependency Injection контейнер.
 *
 * Отличие Koin от Hilt:
 *   - Hilt работает через аннотации (@Inject, @HiltAndroidApp) и генерацию
 *     кода во время компиляции — сложная связка с KSP и Gradle-плагином.
 *   - Koin — это обычная Kotlin-библиотека без аннотаций. Все зависимости
 *     описываются в DSL-модулях и резолвятся в рантайме.
 *
 * Плюсы для нашего проекта:
 *   - Меньше "магии" и автоматики — проще понять и объяснить на защите.
 *   - Нет проблем со сборкой и совместимостью версий.
 *   - Чисто Kotlin-код без преобразований компилятором.
 */
class ChineseDictApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // androidLogger — пишет в Logcat информацию о том, какие
            // зависимости создаются. Полезно при отладке. Level.ERROR
            // означает "выводить только ошибки", чтобы не засорять лог.
            androidLogger(Level.ERROR)

            // androidContext делает Context доступным во всех модулях Koin
            // через get(). Нам пока не нужен, но пригодится для Room в будущем.
            androidContext(this@ChineseDictApplication)

            // Регистрируем все наши DI-модули.
            // Порядок не важен — Koin сам разрешит зависимости между ними.
            modules(
                networkModule,
                dataModule,
                viewModelModule,
            )
        }
    }
}
