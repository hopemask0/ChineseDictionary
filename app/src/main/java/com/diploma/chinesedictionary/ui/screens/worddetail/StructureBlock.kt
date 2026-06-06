package com.diploma.chinesedictionary.ui.screens.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diploma.chinesedictionary.domain.model.CharacterInfo
import com.diploma.chinesedictionary.ui.theme.HskBadgeBg
import com.diploma.chinesedictionary.ui.theme.HskBadgeText
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted

/**
 * Блок "Структура" — этимология иероглифов слова.
 *
 * Для одиночного иероглифа показывает одну карточку.
 * Для составного слова (学习) — отдельную карточку для каждого иероглифа.
 *
 * Каждая карточка содержит:
 *   - Сам иероглиф крупно
 *   - Тип этимологии (пиктограмма/идеограмма/фоносемантика)
 *   - Текстовое объяснение происхождения
 *   - Декомпозицию (компоненты)
 *   - Радикал (ключ)
 *   - Кнопку "Воспроизвести анимацию" (если есть данные штрихов)
 */
@Composable
fun StructureBlock(
    characters: List<CharacterInfo>,
    onCharacterAnimate: (character: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (characters.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Заголовок секции — двуязычный, как в других местах
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "结构",
                style = MaterialTheme.typography.titleMedium,
                color = InkBlack,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "· СТРУКТУРА",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(InkDivider),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // По карточке на каждый иероглиф
        characters.forEachIndexed { index, ch ->
            CharacterStructureCard(
                info = ch,
                onPlayAnimation = { onCharacterAnimate(ch.character) },
            )
            if (index < characters.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CharacterStructureCard(
    info: CharacterInfo,
    onPlayAnimation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        // Верхняя строка: большой иероглиф + кнопка анимации
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Иероглиф крупно
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 1.dp,
                        color = InkDivider,
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = info.character,
                    fontSize = 40.sp,
                    color = InkBlack,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Колонка справа: тип этимологии + пиньинь + кнопка
            Column(modifier = Modifier.weight(1f)) {
                // Тип этимологии — бейдж
                info.etymologyType?.let { type ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HskBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = type.displayName.lowercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = HskBadgeText,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                // Пиньинь
                if (info.pinyin.isNotEmpty()) {
                    Text(
                        text = info.pinyin.joinToString(", "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Кнопка анимации
            if (info.hasStrokes) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onPlayAnimation)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircleOutline,
                        contentDescription = "Анимация написания",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Написать",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        // Этимология (текстовое объяснение)
        if (!info.etymologyHint.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = info.etymologyHint,
                style = MaterialTheme.typography.bodyMedium,
                color = InkBlack.copy(alpha = 0.85f),
            )
        }

        // Декомпозиция + радикал — в строке
        if (info.decomposition != null || info.radical != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                info.decomposition?.let { decomp ->
                    Column {
                        Text(
                            text = "Компоненты",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                        )
                        Text(
                            text = decomp,
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkBlack,
                        )
                    }
                }
                info.radical?.let { radical ->
                    Column {
                        Text(
                            text = "Радикал",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                        )
                        Text(
                            text = radical,
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkBlack,
                        )
                    }
                }
            }
        }
    }
}
