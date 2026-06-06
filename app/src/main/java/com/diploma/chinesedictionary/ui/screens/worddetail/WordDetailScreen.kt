package com.diploma.chinesedictionary.ui.screens.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.domain.model.Example
import com.diploma.chinesedictionary.domain.model.Meaning
import com.diploma.chinesedictionary.domain.model.WordDetail
import com.diploma.chinesedictionary.ui.screens.lists.AddToListBottomSheet
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeBg
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeText
import com.diploma.chinesedictionary.ui.theme.HskBadgeBg
import com.diploma.chinesedictionary.ui.theme.HskBadgeText
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import org.koin.androidx.compose.koinViewModel

@Composable
fun WordDetailScreen(
    onBack: () -> Unit,
    viewModel: WordDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Состояние: какой иероглиф сейчас анимируется (null = диалог закрыт)
    var animatingCharacter by remember { mutableStateOf<String?>(null) }
    // Состояние: открыт ли BottomSheet "добавить в список"
    var showAddToList by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Топ-бар с кнопкой назад и кнопкой "В список"
            // Кнопка "В список" видна только когда слово уже загружено
            TopBar(
                onBack = onBack,
                onAddToList = if (state.word != null) {
                    { showAddToList = true }
                } else null,
            )

            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(
                    error = state.error!!,
                    onRetry = viewModel::retry,
                )
                state.word != null -> WordContent(
                    word = state.word!!,
                    characters = state.characters,
                    onCharacterAnimate = { ch -> animatingCharacter = ch },
                )
            }
        }
    }

    // Диалог анимации — поверх всего, появляется когда animatingCharacter != null
    animatingCharacter?.let { ch ->
        CharacterAnimationDialog(
            character = ch,
            onDismiss = { animatingCharacter = null },
        )
    }

    // BottomSheet добавления в список — открыт когда showAddToList == true
    if (showAddToList && state.word != null) {
        AddToListBottomSheet(
            wordId = state.word!!.id,
            onDismiss = { showAddToList = false },
        )
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onAddToList: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        // Spacer оттолкнёт кнопку добавления к правому краю
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        if (onAddToList != null) {
            IconButton(onClick = onAddToList) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkAdd,
                    contentDescription = "Добавить в список",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Нажмите, чтобы повторить",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun WordContent(
    word: WordDetail,
    characters: List<CharacterInfo>,
    onCharacterAnimate: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // Заголовок: иероглифы крупно
        item { HeroBlock(word) }
        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Значения
        itemsIndexed(word.meanings) { index, meaning ->
            MeaningBlock(meaning = meaning, isFirst = index == 0)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Пометы
        if (word.labels.isNotEmpty()) {
            item { LabelsBlock(labels = word.labels) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Перекрёстные ссылки (см. также)
        if (word.references.isNotEmpty()) {
            item { ReferencesBlock(references = word.references) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Блок "Структура" — этимология иероглифов слова (новое в 4.4)
        if (characters.isNotEmpty()) {
            item {
                StructureBlock(
                    characters = characters,
                    onCharacterAnimate = onCharacterAnimate,
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Финальный отступ снизу
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun HeroBlock(word: WordDetail) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = word.headword,
            fontSize = 72.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 80.sp,
            color = InkBlack,
        )
        if (!word.pinyin.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = word.pinyin,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Бейджи под пиньинь
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            if (word.hskLevel != null) {
                Badge(
                    text = "HSK ${word.hskLevel}",
                    bgColor = HskBadgeBg,
                    textColor = HskBadgeText,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (word.category != "common") {
                Badge(
                    text = categoryLabel(word.category),
                    bgColor = CategoryBadgeBg,
                    textColor = CategoryBadgeText,
                )
            }
        }
    }
}

@Composable
private fun MeaningBlock(meaning: Meaning, isFirst: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Номер значения + текст перевода
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "${meaning.position}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(28.dp),
            )
            Text(
                text = meaning.translation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
        }

        // Примеры
        if (meaning.examples.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            meaning.examples.forEach { example ->
                ExampleItem(example = example)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ExampleItem(example: Example) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = example.chinese,
            style = MaterialTheme.typography.bodyMedium,
            color = InkBlack,
            fontWeight = FontWeight.Medium,
        )
        if (!example.russian.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = example.russian,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun LabelsBlock(labels: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(chineseTitle = "标记", russianTitle = "ПОМЕТЫ")
        Spacer(modifier = Modifier.height(8.dp))
        // Простой Row, в реальном приложении надо использовать FlowRow.
        // Для MVP — Row, переноса не будет если помет много, но они обычно
        // короткие и их 1-3 штуки.
        Row {
            labels.forEach { label ->
                Badge(
                    text = label,
                    bgColor = CategoryBadgeBg,
                    textColor = CategoryBadgeText,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun ReferencesBlock(references: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(chineseTitle = "参见", russianTitle = "СМ. ТАКЖЕ")
        Spacer(modifier = Modifier.height(8.dp))
        references.forEach { ref ->
            Text(
                text = ref,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(chineseTitle: String, russianTitle: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = chineseTitle,
            style = MaterialTheme.typography.titleMedium,
            color = InkBlack,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "· $russianTitle",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(InkDivider),
    )
}

@Composable
private fun Badge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
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