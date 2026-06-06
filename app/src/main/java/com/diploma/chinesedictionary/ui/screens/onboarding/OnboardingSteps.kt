package com.diploma.chinesedictionary.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.Level
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted

/**
 * Все три экрана онбординга получают ОДИН и тот же OnboardingViewModel.
 *
 * Layout каждого экрана:
 *   Column (fillMaxWidth + weight(1f) — занимает всё оставшееся место под заголовком)
 *     ├── content шага (с weight(1f) для прокручиваемой части, если нужно)
 *     └── кнопки внизу
 *
 * Ключевая идея: вертикальный flow внутри ColumnScope OnboardingScaffold.
 * .weight(1f) растягивает Column на всё свободное место.
 */

// ============================================================================
// ШАГ 1 — Имя пользователя
// ============================================================================

@Composable
fun OnboardingNameScreen(
    onNext: () -> Unit,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        currentStep = 1,
        totalSteps = 3,
        chineseTitle = "你好!",
        russianSubtitle = "ПРИВЕТСТВУЕМ · ШАГ 1 ИЗ 3",
    ) {
        // weight(1f) во внутренней Column — внутри ColumnScope OnboardingScaffold,
        // это правильно растягивает её на всё свободное место.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Как нам тебя называть?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Имя") },
                placeholder = { Text("Например, Надя") },
                isError = state.displayNameError != null,
                supportingText = state.displayNameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = InkDivider,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("Логин") },
                placeholder = { Text("латиница, цифры, _") },
                isError = state.usernameError != null,
                supportingText = state.usernameError?.let { { Text(it) } }
                    ?: { Text("Это будет твоим уникальным идентификатором") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = InkDivider,
                ),
            )

            // weight(1f) во вложенном Spacer — оттолкнёт кнопку вниз.
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
                enabled = state.username.isNotBlank()
                        && state.usernameError == null
                        && state.displayName.isNotBlank()
                        && state.displayNameError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "Далее",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
    }
}

// ============================================================================
// ШАГ 2 — Цели изучения
// ============================================================================

@Composable
fun OnboardingGoalsScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        currentStep = 2,
        totalSteps = 3,
        chineseTitle = "目标",
        russianSubtitle = "ЦЕЛИ · ШАГ 2 ИЗ 3",
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Зачем учишь китайский?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Можно выбрать несколько вариантов",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Прокручиваемый список целей с weight(1f) — займёт всё место
            // между заголовком и кнопками.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Goal.entries.forEach { goal ->
                    GoalItem(
                        goal = goal,
                        selected = goal in state.selectedGoals,
                        onClick = { viewModel.onGoalToggle(goal) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Кнопки Назад / Далее
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Назад", color = InkMuted)
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Далее",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalItem(
    goal: Goal,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = goal.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ============================================================================
// ШАГ 3 — Уровень
// ============================================================================

@Composable
fun OnboardingLevelScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        currentStep = 3,
        totalSteps = 3,
        chineseTitle = "水平",
        russianSubtitle = "УРОВЕНЬ · ШАГ 3 ИЗ 3",
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = "Какой у тебя уровень?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Прокручиваемый список уровней
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Level.entries.forEach { level ->
                    LevelItem(
                        level = level,
                        selected = state.selectedLevel == level && !state.wantsToTakeTest,
                        onClick = { viewModel.onLevelSelect(level) },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Вариант "пройти тест"
                TestOptionItem(
                    selected = state.wantsToTakeTest,
                    onClick = viewModel::onWantsToTakeTestToggle,
                )
            }

            // Сообщение об ошибке
            state.submitError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Кнопки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onBack,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Назад", color = InkMuted)
                }
                Button(
                    onClick = { viewModel.submit(onSuccess = onFinish) },
                    enabled = !state.isSubmitting
                            && (state.selectedLevel != null || state.wantsToTakeTest),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Готово",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelItem(
    level: Level,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = level.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (level.hskRange != null) {
                Text(
                    text = level.hskRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }
        RadioDot(selected = selected)
    }
}

@Composable
private fun TestOptionItem(selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Не уверен — пройти тест",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = "Определим уровень автоматически",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
        RadioDot(selected = selected)
    }
}

/**
 * Радио-точка в стиле приложения.
 */
@Composable
private fun RadioDot(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(InkDivider),
        )
    }
}