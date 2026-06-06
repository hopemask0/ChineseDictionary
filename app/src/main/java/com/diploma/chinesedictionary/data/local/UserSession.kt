package com.diploma.chinesedictionary.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Локальное хранение информации о текущем пользователе.
 *
 * Использует EncryptedSharedPreferences — это специальный SharedPreferences,
 * который шифрует значения через системный Android Keystore. Ключи шифрования
 * хранятся в защищённой области ОС и недоступны другим приложениям.
 *
 * Что мы храним:
 *   - user_id  — числовой идентификатор для подстановки в X-User-Id
 *   - username — для отображения и подсказки при выходе
 *
 * Чего мы НЕ храним:
 *   - Цели и уровень — это получаем с сервера через /auth/me
 *     (single source of truth — данные живут на сервере)
 *
 * Это синглтон — один экземпляр на всё приложение. Создаётся через Koin.
 */
class UserSession(context: Context) {

    private val prefs: SharedPreferences = run {
        // MasterKey — это ключ, которым зашифрованы все значения в этом
        // хранилище. Он сам зашифрован системным Keystore и недоступен
        // другим приложениям.
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            // Схема для ключей (имён настроек) — оставляем стандартную.
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            // Схема для значений — AES-256-GCM (промышленный стандарт).
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Сохранить данные пользователя локально после регистрации/входа.
     */
    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    /**
     * Получить сохранённый user_id или null, если пользователь не залогинен.
     *
     * Используем -1 как "магическое значение отсутствия" — getInt не умеет
     * вернуть null, а Int? в SharedPreferences не поддерживается.
     */
    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id == -1) null else id
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    /**
     * Проверка: залогинен ли кто-то.
     * Аналог `if (currentUser != null)`.
     */
    fun isLoggedIn(): Boolean = getUserId() != null

    /**
     * Выход: удаляем сохранённые данные.
     * После этого приложение при следующем запуске покажет онбординг.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val FILE_NAME = "user_session_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
}
