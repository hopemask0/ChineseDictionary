package com.diploma.chinesedictionary.ui.screens.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.TrainerCard
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import org.koin.androidx.compose.koinViewModel

/**
 * Главный экран тренажёра.
 *
 * UX:
 *   - Сверху прогресс-бар (3/12, в процентах)
 *   - В центре карточка: показан иероглиф, под ним кнопка "Показать ответ"
 *   - После переворота показываются пиньинь и перевод
 *   - Внизу две кнопки: "Учу" (красная) и "Знаю" (зелёная)
 *   - Когда все слова пройдены — показывается итоговый экран
 *
 * "Переворот" сделан простым fade-переходом через AnimatedVisibility —
 * это надёжнее, чем сложная 3D анимация, и хорошо смотрится.
 */
@Composable
fun TrainerScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: TrainerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> ErrorScreen(state.error!!)
            state.isFinished -> FinishScreen(
                listTitle = state.listTitle,
                known = state.knownInSession,
                learning = state.learningInSession,
                total = state.cards.size,
                onRestart = viewModel::restart,
                onFinish = onFinish,
            )
            state.currentCard != null -> TrainingContent(
                card = state.currentCard!!,
                currentIndex = state.currentIndex,
                totalCount = state.cards.size,
                progress = state.progress,
                showAnswer = state.showAnswer,
                onFlip = viewModel::flipCard,
                onAnswer = viewModel::answer,
                onClose = onBack,
            )
        }
    }
}

// ============================================================================
// Контент тренировки — одна карточка
// ============================================================================

@Composable
private fun TrainingContent(
    card: TrainerCard,
    currentIndex: Int,
    totalCount: Int,
    progress: Float,
    showAnswer: Boolean,
    onFlip: () -> Unit,
    onAnswer: (knows: Boolean) -> Unit,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Топ-бар с кнопкой закрытия и прогрессом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Прервать тренировку",
                    tint = InkBlack,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = InkRed,
                    trackColor = InkDivider,
                )
                Text(
                    text = "Карточка ${currentIndex + 1} из $totalCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = 4.dp, end = 12.dp),
                )
            }
            Spacer(modifier = Modifier.width(48.dp))  // место под симметрию
        }

        // Карточка слова — занимает центральную часть
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            FlashCard(card = card, showAnswer = showAnswer)
        }

        // Кнопки внизу
        if (showAnswer) {
            // Две кнопки: "Учу" и "Знаю"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnswerButton(
                    text = "✗ Учу",
                    color = InkRed,
                    onClick = { onAnswer(false) },
                    modifier = Modifier.weight(1f),
                )
                AnswerButton(
                    text = "✓ Знаю",
                    color = Color(0xFF388E3C),  // зелёный — позитивное действие
                    onClick = { onAnswer(true) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            // Одна кнопка: "Показать ответ"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                AnswerButton(
                    text = "Показать ответ",
                    color = InkBlack,
                    onClick = onFlip,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Сама карточка слова.
 * Спереди — крупный иероглиф (всегда виден).
 * Снизу — пиньинь и перевод (видны после переворота).
 */
@Composable
private fun FlashCard(card: TrainerCard, showAnswer: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Иероглиф крупно — всегда виден
            Text(
                text = card.headword,
                fontSize = 72.sp,
                fontWeight = FontWeight.Normal,
                color = InkRed,
                lineHeight = 84.sp,
            )

            // Если слово уже выученное — маленькая плашка
            if (card.isLearned) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PorcelainBlue)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "✓ выучено",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkBlack,
                    )
                }
            }

            // Пиньинь и перевод — плавно появляются при перевороте
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = showAnswer,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Разделитель
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(1.dp)
                            .background(InkDivider),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!card.pinyin.isNullOrBlank()) {
                        Text(
                            text = card.pinyin,
                            style = MaterialTheme.typography.headlineSmall,
                            color = InkBlack,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!card.translation.isNullOrBlank()) {
                        Text(
                            text = card.translation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkBlack,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 14.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ============================================================================
// Загрузка / ошибка
// ============================================================================

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = InkRed)
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, color = InkRed)
    }
}

// ============================================================================
// Финальный экран — статистика тренировки
// ============================================================================

@Composable
private fun FinishScreen(
    listTitle: String,
    known: Int,
    learning: Int,
    total: Int,
    onRestart: () -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Большая эмодзи-плашка
        Text(text = "🎉", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "完成",
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkRed,
        )
        Text(
            text = "ТРЕНИРОВКА ЗАВЕРШЕНА",
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = listTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = InkBlack,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Статистика
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StatRow("Всего карточек", total.toString(), InkBlack)
                Spacer(modifier = Modifier.height(8.dp))
                StatRow("Знаю", known.toString(), Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(8.dp))
                StatRow("Учу", learning.toString(), InkRed)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnswerButton(
                text = "К списку",
                color = InkBlack,
                onClick = onFinish,
                modifier = Modifier.weight(1f),
            )
            AnswerButton(
                text = "Ещё раз",
                color = InkRed,
                onClick = onRestart,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = InkMuted, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
