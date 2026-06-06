package com.diploma.chinesedictionary.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.Level
import com.diploma.chinesedictionary.domain.model.User
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.ui.theme.HskBadgeBg
import com.diploma.chinesedictionary.ui.theme.HskBadgeText
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import org.koin.androidx.compose.koinViewModel

/**
 * Главный экран приложения.
 *
 * Содержит несколько карточек:
 *   1. Приветствие с временем суток
 *   2. Слово дня (большая герой-карточка)
 *   3. Прогресс (заглушка)
 *   4. Культурный календарь (заглушка)
 *   5. Контекстная подборка (заглушка)
 *   6. Уровень пользователя
 *   7. Быстрые действия
 */
@Composable
fun HomeScreen(
    onWordOfTheDayClick: (wordId: Int) -> Unit,
    onSearchClick: () -> Unit,
    onListsClick: () -> Unit,
    onTrainerClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Приветствие
            GreetingCard(
                chinese = state.greeting.chinese,
                pinyin = state.greeting.pinyin,
                russian = state.greeting.russian,
                userName = state.user?.displayName,
            )

            // 2. Слово дня (главный блок-герой)
            WordOfTheDayCard(
                word = state.wordOfTheDay,
                isReloading = state.isReloadingWord,
                onClick = {
                    state.wordOfTheDay?.let { onWordOfTheDayClick(it.id) }
                },
                onReload = viewModel::reloadWordOfTheDay,
            )

            // 3. Прогресс (заглушка)
            ProgressCardStub()

            // 4 + 5: две карточки рядом — культурный календарь и контекстная подборка
            // (на узких экранах они могут переноситься на новую строку,
            //  но для MVP оставляем простой Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CulturalCalendarStub(modifier = Modifier.weight(1f))
                TravelerPackStub(modifier = Modifier.weight(1f))
            }

            // 6. Уровень
            LevelCard(level = state.user?.level)

            // 7. Быстрые действия
            QuickActionsCard(
                onSearchClick = onSearchClick,
                onListsClick = onListsClick,
                onTrainerClick = onTrainerClick,
            )
        }
    }
}

// ============================================================================
// 1. Приветствие
// ============================================================================

@Composable
private fun GreetingCard(
    chinese: String,
    pinyin: String,
    russian: String,
    userName: String?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Большая иероглифическая надпись
        Text(
            text = chinese,
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkBlack,
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Пиньинь и перевод в одной строке через "·"
        Text(
            text = buildString {
                append(pinyin)
                append(" · ")
                append(russian)
                if (!userName.isNullOrBlank()) {
                    append(", ")
                    append(userName)
                }
                append("!")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
        )
    }
}

// ============================================================================
// 2. Слово дня — большая герой-карточка
// ============================================================================

@Composable
private fun WordOfTheDayCard(
    word: WordDetail?,
    isReloading: Boolean,
    onClick: () -> Unit,
    onReload: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = word != null, onClick = onClick)
            .padding(20.dp),
    ) {
        // Заголовок раздела
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "今日字",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "СЛОВО ДНЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                )
            }
            // Кнопка "обновить слово"
            IconButton(
                onClick = onReload,
                enabled = !isReloading,
            ) {
                if (isReloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = InkMuted,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Обновить",
                        tint = InkMuted,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Само слово
        if (word != null) {
            Text(
                text = word.headword,
                fontSize = 64.sp,
                fontWeight = FontWeight.Normal,
                color = InkBlack,
                lineHeight = 72.sp,
            )
            if (!word.pinyin.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = word.pinyin,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            // Первый перевод
            val firstMeaning = word.meanings.firstOrNull()?.translation
            if (!firstMeaning.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = firstMeaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkBlack.copy(alpha = 0.85f),
                )
            }
            // Подсказка "нажми, чтобы открыть"
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Открыть карточку",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            // Состояние "ещё не загрузилось"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

// ============================================================================
// 3. Прогресс (заглушка)
// ============================================================================

@Composable
private fun ProgressCardStub() {
    PlaceholderCard(
        chineseTitle = "进步",
        russianTitle = "ПРОГРЕСС",
        description = "Скоро: учёт изученных слов",
        modifier = Modifier.fillMaxWidth(),
    )
}

// ============================================================================
// 4. Культурный календарь (заглушка)
// ============================================================================

@Composable
private fun CulturalCalendarStub(modifier: Modifier = Modifier) {
    PlaceholderCard(
        chineseTitle = "文化",
        russianTitle = "КАЛЕНДАРЬ",
        description = "Праздники, традиции, фестивали",
        icon = Icons.Outlined.CalendarMonth,
        modifier = modifier,
    )
}

// ============================================================================
// 5. Контекстная подборка (заглушка)
// ============================================================================

@Composable
private fun TravelerPackStub(modifier: Modifier = Modifier) {
    PlaceholderCard(
        chineseTitle = "旅行",
        russianTitle = "В ПУТЬ",
        description = "Подборки для путешественников",
        icon = Icons.Outlined.Explore,
        modifier = modifier,
    )
}

// ============================================================================
// Общий компонент для заглушек
// ============================================================================

@Composable
private fun PlaceholderCard(
    chineseTitle: String,
    russianTitle: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = InkDivider,
                shape = RoundedCornerShape(16.dp),
            )
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chineseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = russianTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                )
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ComingSoonBadge()
    }
}

@Composable
private fun ComingSoonBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HskBadgeBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = "скоро",
            style = MaterialTheme.typography.labelSmall,
            color = HskBadgeText,
        )
    }
}

// ============================================================================
// 6. Уровень пользователя
// ============================================================================

@Composable
private fun LevelCard(level: Int?) {
    val levelEnum = Level.fromCode(level)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "水平",
                style = MaterialTheme.typography.titleSmall,
                color = InkMuted,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = "ТВОЙ УРОВЕНЬ",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = levelEnum?.displayName ?: "Не определён",
                style = MaterialTheme.typography.titleLarge,
                color = InkBlack,
                fontWeight = FontWeight.SemiBold,
            )
            if (levelEnum?.hskRange != null) {
                Text(
                    text = levelEnum.hskRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            } else if (levelEnum == null) {
                Text(
                    text = "Пройди тест в разделе тренажёра",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }
    }
}

// ============================================================================
// 7. Быстрые действия
// ============================================================================

@Composable
private fun QuickActionsCard(
    onSearchClick: () -> Unit,
    onListsClick: () -> Unit,
    onTrainerClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Действия",
            style = MaterialTheme.typography.labelLarge,
            color = InkMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        // Сетка 2x2
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    icon = Icons.Outlined.Search,
                    label = "Найти слово",
                    enabled = true,
                    onClick = onSearchClick,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    icon = Icons.Outlined.AutoStories,
                    label = "Списки",
                    enabled = true,
                    onClick = onListsClick,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    icon = Icons.Outlined.Refresh,
                    label = "Тренажёр",
                    enabled = true,
                    onClick = onTrainerClick,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    icon = Icons.Outlined.Chat,
                    label = "ИИ-чат",
                    enabled = false,
                    onClick = { },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor: Color
    val contentColor: Color
    if (enabled) {
        containerColor = MaterialTheme.colorScheme.primary
        contentColor = MaterialTheme.colorScheme.onPrimary
    } else {
        containerColor = MaterialTheme.colorScheme.surface
        contentColor = InkMuted
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
        if (!enabled) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "скоро",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                fontSize = 9.sp,
            )
        }
    }
}