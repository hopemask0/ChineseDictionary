package com.diploma.chinesedictionary.ui.screens.lists

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.UserListItem
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import org.koin.androidx.compose.koinViewModel

@Composable
fun ListDetailScreen(
    onBack: () -> Unit,
    onWordClick: (wordId: Int) -> Unit,
    onStartTraining: (listId: Int) -> Unit,
    viewModel: ListDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Top bar
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
                Spacer(modifier = Modifier.weight(1f))
                // Кнопка "Тренироваться" — только если список загружен и не пуст
                val detail = state.detail
                if (detail != null && detail.items.isNotEmpty()) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onStartTraining(detail.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = InkRed,
                    ) {
                        Text(
                            text = "▶ Тренировка",
                            color = androidx.compose.ui.graphics.Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InkRed)
                }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error ?: "",
                        color = InkRed,
                    )
                }

                state.detail != null -> ListDetailContent(
                    detail = state.detail!!,
                    onWordClick = onWordClick,
                    onRemoveWord = viewModel::removeWord,
                )
            }
        }
    }
}

@Composable
private fun ListDetailContent(
    detail: com.diploma.chinesedictionary.domain.model.UserListDetail,
    onWordClick: (Int) -> Unit,
    onRemoveWord: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        // Заголовок списка
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PorcelainBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = detail.icon ?: "📋", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = InkBlack,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${detail.items.size} слов в списке",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        if (detail.items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Список пуст.\nДобавляйте слова из карточки слова.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            items(detail.items, key = { it.wordId }) { item ->
                WordItemRow(
                    item = item,
                    onClick = { onWordClick(item.wordId) },
                    onRemove = { onRemoveWord(item.wordId) },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InkDivider),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun WordItemRow(
    item: UserListItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Иероглиф крупно красным (печать)
        Text(
            text = item.headword,
            fontSize = 26.sp,
            fontWeight = FontWeight.Normal,
            color = InkRed,
            modifier = Modifier.width(80.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            if (!item.pinyin.isNullOrBlank()) {
                Text(
                    text = item.pinyin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkBlack,
                )
            }
            if (!item.firstTranslation.isNullOrBlank()) {
                Text(
                    text = item.firstTranslation,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Удалить из списка",
                tint = InkMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}