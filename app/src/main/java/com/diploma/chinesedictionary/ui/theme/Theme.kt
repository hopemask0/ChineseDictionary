package com.diploma.chinesedictionary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Главная тема приложения.
 *
 * Передаём в MaterialTheme нашу цветовую схему и типографику. После этого
 * все стандартные Material 3 компоненты (Button, Card, TextField) будут
 * автоматически использовать наши цвета.
 *
 * Тёмную тему пока не делаем — это будет в одной из следующих итераций.
 * Для дипломного MVP достаточно светлой.
 */

private val AppLightColorScheme = lightColorScheme(
    primary = InkRed,
    onPrimary = InkPaper,
    primaryContainer = HskBadgeBg,
    onPrimaryContainer = InkRed,

    secondary = InkMuted,
    onSecondary = InkBlack,

    background = InkPaper,
    onBackground = InkBlack,

    surface = InkPaperRaised,
    onSurface = InkBlack,
    surfaceVariant = HskBadgeBg,
    onSurfaceVariant = HskBadgeText,

    outline = InkDivider,
    outlineVariant = InkDivider,
)

@Composable
fun ChineseDictionaryTheme(
    @Suppress("UNUSED_PARAMETER")
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Пока используем светлую тему даже в тёмном режиме системы.
    // В будущем здесь будет проверка darkTheme и выбор соответствующей схемы.
    val colorScheme = AppLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
