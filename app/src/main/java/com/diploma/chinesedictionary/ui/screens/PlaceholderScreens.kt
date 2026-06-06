package com.diploma.chinesedictionary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diploma.chinesedictionary.ui.theme.InkMuted

/**
 * Заглушки для экранов, которые ещё не реализованы.
 *
 * План итераций:
 *   - Итерация 5 — списки слов
 *   - Итерация 6+ — тренажёр-карточки
 *   - Итерация 7 — ИИ-диалог
 *
 * HomeScreen теперь реальный — см. ui/screens/home/
 * ProfileScreen теперь реальный — см. ui/screens/profile/
 */

@Composable
fun PlaceholderScreen(
    chineseTitle: String,
    russianTitle: String,
    description: String,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = chineseTitle,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = russianTitle,
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
            )
        }
    }
}

@Composable
fun ListsScreen() = PlaceholderScreen(
    chineseTitle = "词单",
    russianTitle = "СПИСКИ",
    description = "Управление пользовательскими списками — скоро",
)

@Composable
fun DialogScreen() = PlaceholderScreen(
    chineseTitle = "对话",
    russianTitle = "ДИАЛОГ С ИИ",
    description = "Практика диалогов с ИИ-ассистентом — скоро",
)