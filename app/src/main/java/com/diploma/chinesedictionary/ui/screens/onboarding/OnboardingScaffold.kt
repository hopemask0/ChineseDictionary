package com.diploma.chinesedictionary.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted

/**
 * Каркас экрана онбординга: шаги-индикатор сверху, заголовок, контент снизу.
 *
 * ВАЖНАЯ ДЕТАЛЬ: content передаётся как функция-расширение от ColumnScope
 * (content: @Composable ColumnScope.() -> Unit). Это даёт content'у доступ
 * к Modifier.weight(1f), который работает только внутри Column/Row.
 *
 * Без ColumnScope.() в типе функции, content() не сможет использовать
 * .weight() для растягивания на остаточное пространство — клики не
 * будут доходить до элементов.
 */
@Composable
fun OnboardingScaffold(
    currentStep: Int,
    totalSteps: Int,
    chineseTitle: String,
    russianSubtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Индикатор прогресса
            StepIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps,
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
            )

            // Заголовок
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(
                    text = chineseTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = russianSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                )
            }

            // Контент шага — благодаря ColumnScope он получит доступ к weight()
            content()
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        for (i in 1..totalSteps) {
            val isActive = i <= currentStep
            Box(
                modifier = Modifier
                    .width(if (i == currentStep) 32.dp else 16.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else InkDivider
                    ),
            )
            if (i < totalSteps) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}