package com.diploma.chinesedictionary.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.ui.screens.lists.ListsViewModel
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import org.koin.androidx.compose.koinViewModel

/**
 * Экран выбора списка для тренировки.
 *
 * Показывает все списки пользователя. По нажатию на любой — запуск
 * тренировки по этому списку.
 *
 * Переиспользуем ListsViewModel — он уже умеет загружать списки.
 */
@Composable
fun TrainerListPickerScreen(
    onBack: () -> Unit,
    onStartTraining: (listId: Int) -> Unit,
    viewModel: ListsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar с заголовком
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Назад",
                        tint = InkBlack,
                    )
                }
            }

            // Заголовок раздела
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "练习",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkRed,
                )
                Text(
                    text = "ТРЕНАЖЁР · ВЫБЕРИТЕ СПИСОК",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                )
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InkRed)
                }

                state.myLists.isEmpty() -> EmptyState()

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.myLists, key = { it.id }) { list ->
                        TrainableListCard(
                            list = list,
                            onClick = {
                                // Тренируем только непустые списки
                                if (list.wordCount > 0) {
                                    onStartTraining(list.id)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "📚",
            fontSize = 48.sp,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "У вас пока нет списков для тренировки",
            style = MaterialTheme.typography.titleMedium,
            color = InkBlack,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Создайте список или подпишитесь на готовый в разделе Списки",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun TrainableListCard(
    list: UserList,
    onClick: () -> Unit,
) {
    val isEnabled = list.wordCount > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PorcelainBlue),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = list.icon ?: "📋", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEnabled) InkBlack else InkMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isEnabled) {
                        "${list.wordCount} слов · нажмите чтобы тренироваться"
                    } else "Пустой список",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
            if (isEnabled) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = InkRed,
                )
            }
        }
    }
}
