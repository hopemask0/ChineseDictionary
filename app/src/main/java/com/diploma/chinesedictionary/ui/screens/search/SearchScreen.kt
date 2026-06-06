package com.diploma.chinesedictionary.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.WordSummary
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeBg
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeText
import com.diploma.chinesedictionary.ui.theme.HskBadgeBg
import com.diploma.chinesedictionary.ui.theme.HskBadgeText
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import org.koin.androidx.compose.koinViewModel

/**
 * Экран поиска слов.
 *
 * Архитектура:
 *   - Этот файл — "тупой" UI, который только отображает состояние и
 *     передаёт события в ViewModel.
 *   - Вся логика поиска в SearchViewModel.
 *
 * Compose-подход: функции с аннотацией @Composable описывают UI декларативно.
 * Когда состояние меняется — Compose автоматически перерисовывает только то,
 * что зависит от изменившегося значения. Никакого RecyclerView, адаптеров и
 * notifyDataSetChanged.
 *
 * Параметр onWordClick — коллбэк для перехода на карточку слова.
 * Сам переход реализован в навигации, экран лишь "сигналит" о клике.
 */
@Composable
fun SearchScreen(
    onWordClick: (wordId: Int) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    // Подписываемся на состояние ViewModel. collectAsStateWithLifecycle
    // автоматически отписывается, когда экран уходит в фон — экономим ресурсы.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // === Заголовок раздела в стилистике "иероглиф · РУССКИЙ" ===
            SectionHeader()

            // === Поле поиска ===
            SearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onClear = viewModel::onClearQuery,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            // === Подсказка под полем (количество результатов или ошибка) ===
            ResultsCounter(
                isLoading = state.isLoading,
                total = state.total,
                elapsedMs = state.elapsedMs,
                hasQuery = state.query.isNotBlank(),
                error = state.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            // === Список результатов ===
            SearchResultsList(
                items = state.items,
                onWordClick = onWordClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SectionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // Главный заголовок: иероглиф крупно
        Text(
            text = "词典",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Подзаголовок мелко с трекингом — двуязычность как фишка
        Text(
            text = "СЛОВАРЬ · СЛОВО",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted,
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Иероглиф, пиньинь или перевод",
                color = InkMuted,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Поиск",
                tint = InkMuted,
            )
        },
        trailingIcon = {
            // Кнопка очистки показывается только когда есть ввод
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Очистить",
                        tint = InkMuted,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = InkDivider,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun ResultsCounter(
    isLoading: Boolean,
    total: Int,
    elapsedMs: Float,
    hasQuery: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            error != null -> {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ищу...",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
            hasQuery && total > 0 -> {
                Text(
                    text = "Найдено: $total · за ${"%.1f".format(elapsedMs)} мс",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
            hasQuery -> {
                Text(
                    text = "Ничего не найдено",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
            else -> {
                Text(
                    text = "Начните вводить слово",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    items: List<WordSummary>,
    onWordClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
    ) {
        items(
            items = items,
            key = { it.id },  // ключ для стабильной идентификации в Compose
        ) { word ->
            WordSummaryItem(
                word = word,
                onClick = { onWordClick(word.id) },
            )
            HorizontalDivider(
                color = InkDivider,
                thickness = 0.5.dp,
            )
        }
    }
}

/**
 * Одна строка в списке результатов поиска.
 *
 * Слева: крупный иероглиф + пиньинь
 * Справа: HSK-бейдж (если есть) и категория (если не common)
 * Снизу: короткий перевод
 */
@Composable
private fun WordSummaryItem(
    word: WordSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 14.dp, horizontal = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Иероглифы
            Text(
                text = word.headword,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Пиньинь
            if (!word.pinyin.isNullOrBlank()) {
                Text(
                    text = word.pinyin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Бейджи справа
            if (word.hskLevel != null) {
                Badge(
                    text = "HSK ${word.hskLevel}",
                    bgColor = HskBadgeBg,
                    textColor = HskBadgeText,
                )
            }
            if (word.category != "common") {
                Spacer(modifier = Modifier.width(6.dp))
                Badge(
                    text = categoryLabel(word.category),
                    bgColor = CategoryBadgeBg,
                    textColor = CategoryBadgeText,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = word.shortTranslation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

@Composable
private fun Badge(
    text: String,
    bgColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "term" -> "термин"
    "proper_noun" -> "имя собств."
    "rare" -> "редкое"
    else -> category
}