package com.diploma.chinesedictionary.ui.screens.leveltest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.TestQuestion
import com.diploma.chinesedictionary.domain.model.TestQuestionType
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import org.koin.androidx.compose.koinViewModel

/**
 * Экран входного HSK-теста. Один большой Composable, который меняет содержимое
 * в зависимости от фазы (см. LevelTestViewModel.TestPhase).
 *
 * Фазы:
 *   INTRO     — стартовый экран с описанием
 *   LOADING   — спиннер
 *   QUESTION  — собственно вопрос (один из трёх типов)
 *   FEEDBACK  — обратная связь "правильно/неправильно" между вопросами
 *   FINISHED  — итоговый экран с уровнем и статистикой
 *   ERROR     — экран ошибки
 */
@Composable
fun LevelTestScreen(
    onExit: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: LevelTestViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (state.phase) {
            TestPhase.INTRO -> IntroPhase(
                onStart = viewModel::start,
                onExit = onExit,
            )
            TestPhase.LOADING -> LoadingPhase()
            TestPhase.QUESTION -> QuestionPhase(
                state = state,
                onAnswerChange = viewModel::updateAnswer,
                onSubmit = viewModel::submitAnswer,
                onExit = onExit,
            )
            TestPhase.FEEDBACK -> FeedbackPhase(
                state = state,
                onNext = viewModel::nextQuestion,
                onExit = onExit,
            )
            TestPhase.FINISHED -> FinishedPhase(
                state = state,
                onSave = { viewModel.saveAndExit(onCompleted) },
                onExit = onExit,
            )
            TestPhase.ERROR -> ErrorPhase(
                message = state.errorMessage ?: "Ошибка",
                onRetry = viewModel::start,
                onExit = onExit,
            )
        }
    }
}

// ============================================================================
// INTRO
// ============================================================================

@Composable
private fun IntroPhase(onStart: () -> Unit, onExit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopCloseBar(onExit = onExit)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🎯", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "水平测试",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkRed,
            )
            Text(
                text = "ОПРЕДЕЛИТЕ СВОЙ УРОВЕНЬ",
                style = MaterialTheme.typography.labelMedium,
                color = InkMuted,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Тест адаптивно подстроится под ваш уровень. " +
                        "На каждом этапе — 10 вопросов разных типов. " +
                        "Для перехода на следующий уровень нужно ответить " +
                        "правильно хотя бы на 60%.",
                style = MaterialTheme.typography.bodyLarge,
                color = InkBlack,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(text = "Начать тест", onClick = onStart)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Можно прервать в любой момент",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
            )
        }
    }
}

// ============================================================================
// LOADING
// ============================================================================

@Composable
private fun LoadingPhase() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = InkRed)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Загрузка вопросов...", color = InkMuted)
        }
    }
}

// ============================================================================
// QUESTION — главный экран с вопросом
// ============================================================================

@Composable
private fun QuestionPhase(
    state: LevelTestUiState,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit,
) {
    val question = state.currentQuestion ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Топ-бар: закрыть + прогресс по уровню
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Outlined.Close, contentDescription = "Прервать", tint = InkBlack)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HSK ${state.currentHskLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkRed,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${state.currentQuestionIndex + 1}/${state.totalOnCurrentLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkMuted,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.levelProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = InkRed,
                        trackColor = InkDivider,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            QuestionContent(
                question = question,
                pendingAnswer = state.pendingAnswer,
                onAnswerChange = onAnswerChange,
            )
        }

        // Кнопка "Ответить"
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            PrimaryButton(
                text = "Ответить",
                onClick = onSubmit,
                enabled = state.pendingAnswer.isNotBlank(),
            )
        }
    }
}

@Composable
private fun QuestionContent(
    question: TestQuestion,
    pendingAnswer: String,
    onAnswerChange: (String) -> Unit,
) {
    when (question.type) {
        TestQuestionType.CHOOSE_TRANSLATION -> {
            // Показан иероглиф + пиньинь, выбрать перевод
            HanziPromptCard(question)
            QuestionLabel("Выберите перевод:")
            ChoicesList(
                choices = question.choices,
                selected = pendingAnswer,
                onSelect = onAnswerChange,
            )
        }
        TestQuestionType.CHOOSE_HANZI -> {
            // Показан перевод, выбрать иероглиф
            TranslationPromptCard(question.correctAnswer)
            QuestionLabel("Выберите иероглиф:")
            HanziChoicesList(
                choices = question.choices,
                selected = pendingAnswer,
                onSelect = onAnswerChange,
            )
        }
        TestQuestionType.TYPE_TRANSLATION -> {
            // Показан иероглиф + пиньинь, ввести перевод
            HanziPromptCard(question)
            QuestionLabel("Введите перевод:")
            OutlinedTextField(
                value = pendingAnswer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Перевод") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                ),
            )
        }
    }
}

@Composable
private fun HanziPromptCard(question: TestQuestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = question.hanzi,
                fontSize = 56.sp,
                color = InkRed,
                fontWeight = FontWeight.Normal,
            )
            if (!question.pinyin.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question.pinyin,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack,
                )
            }
        }
    }
}

@Composable
private fun TranslationPromptCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = InkBlack,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuestionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = InkMuted,
    )
}

@Composable
private fun ChoicesList(
    choices: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        choices.forEach { choice ->
            ChoiceRow(text = choice, isSelected = choice == selected, onClick = { onSelect(choice) })
        }
    }
}

@Composable
private fun HanziChoicesList(
    choices: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    // Иероглифы рендерим крупнее
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        choices.forEach { choice ->
            HanziChoiceRow(text = choice, isSelected = choice == selected, onClick = { onSelect(choice) })
        }
    }
}

@Composable
private fun ChoiceRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PorcelainBlue else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) InkRed else InkDivider,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = InkBlack,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun HanziChoiceRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PorcelainBlue else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) InkRed else InkDivider,
        ),
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            color = InkRed,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

// ============================================================================
// FEEDBACK — между вопросами
// ============================================================================

@Composable
private fun FeedbackPhase(
    state: LevelTestUiState,
    onNext: () -> Unit,
    onExit: () -> Unit,
) {
    val question = state.currentQuestion ?: return
    val isCorrect = state.lastAnswerCorrect == true

    Column(modifier = Modifier.fillMaxSize()) {
        TopCloseBar(onExit = onExit)

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (isCorrect) "✓" else "✗",
                fontSize = 80.sp,
                color = if (isCorrect) Color(0xFF388E3C) else InkRed,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isCorrect) "Правильно!" else "Неправильно",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isCorrect) Color(0xFF388E3C) else InkRed,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Правильный ответ
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = question.hanzi,
                        fontSize = 36.sp,
                        color = InkRed,
                    )
                    if (!question.pinyin.isNullOrBlank()) {
                        Text(
                            text = question.pinyin,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkBlack,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Для CHOOSE_HANZI правильным ответом был иероглиф (он уже
                    // показан выше большим шрифтом), а correctAnswer хранит
                    // перевод. Поэтому показываем correctAnswer (= перевод)
                    // как поясняющую информацию о слове.
                    Text(
                        text = if (question.type == TestQuestionType.CHOOSE_HANZI) {
                            "Перевод: ${question.correctAnswer}"
                        } else {
                            "Правильный ответ: ${question.correctAnswer}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Дальше →", onClick = onNext)
        }
    }
}

// ============================================================================
// FINISHED — финал теста
// ============================================================================

@Composable
private fun FinishedPhase(
    state: LevelTestUiState,
    onSave: () -> Unit,
    onExit: () -> Unit,
) {
    val determined = state.determinedLevel ?: 1

    Column(modifier = Modifier.fillMaxSize()) {
        TopCloseBar(onExit = onExit)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🎉", fontSize = 72.sp)

            Text(
                text = "完成",
                fontSize = 28.sp,
                color = InkRed,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "ВАШ УРОВЕНЬ",
                style = MaterialTheme.typography.labelMedium,
                color = InkMuted,
            )

            // Большой бейдж уровня
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = InkRed,
            ) {
                Text(
                    text = "HSK $determined",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 16.dp),
                )
            }

            // Статистика по каждому пройденному уровню
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Статистика ответов",
                style = MaterialTheme.typography.labelLarge,
                color = InkMuted,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.answersPerLevel.toSortedMap().forEach { (level, stats) ->
                        val (correct, total) = stats
                        val percent = if (total > 0) (correct * 100 / total) else 0
                        val passed = percent >= 60
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "HSK $level",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkBlack,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(80.dp),
                            )
                            Text(
                                text = "$correct из $total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (passed) Color(0xFF388E3C) else InkRed,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.errorMessage != null) {
                Text(text = state.errorMessage, color = InkRed)
            }

            if (state.isSaving) {
                CircularProgressIndicator(color = InkRed)
            } else {
                PrimaryButton(text = "Сохранить и выйти", onClick = onSave)
            }
        }
    }
}

// ============================================================================
// ERROR
// ============================================================================

@Composable
private fun ErrorPhase(message: String, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopCloseBar(onExit = onExit)
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "⚠️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = InkRed, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(text = "Попробовать снова", onClick = onRetry)
        }
    }
}

// ============================================================================
// Общие компоненты
// ============================================================================

@Composable
private fun TopCloseBar(onExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onExit) {
            Icon(Icons.Outlined.Close, contentDescription = "Закрыть", tint = InkBlack)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) InkRed else InkRed.copy(alpha = 0.4f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
        )
    }
}