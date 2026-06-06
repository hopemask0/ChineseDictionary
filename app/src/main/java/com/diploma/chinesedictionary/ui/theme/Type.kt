package com.diploma.chinesedictionary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографика приложения.
 *
 * В будущей итерации можно подключить кастомные шрифты:
 *   - Для иероглифов — Noto Serif CJK SC или Source Han Serif
 *   - Для русского — DM Sans или Manrope
 *   - Для пиньинь — DM Mono
 *
 * Сейчас используем системный шрифт (FontFamily.Default), который на
 * Android по умолчанию хорошо отображает китайские иероглифы через
 * системный fallback к Noto Sans CJK.
 *
 * Семантика стилей по Material 3:
 *   - displayLarge/Medium/Small — для очень крупных заголовков (иероглифы карточки)
 *   - headlineLarge/Medium/Small — заголовки экранов и секций
 *   - titleLarge/Medium/Small — заголовки карточек, элементов списка
 *   - bodyLarge/Medium/Small — основной текст
 *   - labelLarge/Medium/Small — кнопки, бейджи, метки
 */
val AppTypography = Typography(
    // === Очень крупные — для иероглифов на карточке ===
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 96.sp,
        lineHeight = 104.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 72.sp,
    ),

    // === Заголовки ===
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),

    // === Иероглифы в списке результатов ===
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),

    // === Основной текст: переводы, примеры ===
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // === Метки и бейджи ===
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,  // широко расставленные буквы для двуязычных лейблов
    ),
)
