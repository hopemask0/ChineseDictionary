package com.diploma.chinesedictionary.ui.screens.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Модальная "шторка снизу" для добавления слова в список.
 *
 * Используется на карточке слова: пользователь нажимает "+ в список",
 * открывается шторка со всеми его списками. Можно кликнуть на любой,
 * и слово добавится. Внизу — кнопка "Создать новый список".
 *
 * ModalBottomSheet — это Material 3 компонент, специально для таких UX.
 * Свайп вниз закрывает; клик на фон тоже закрывает.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToListBottomSheet(
    wordId: Int,
    onDismiss: () -> Unit,
    viewModel: AddToListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // Заголовок шторки
            Text(
                text = "Добавить в список",
                style = MaterialTheme.typography.titleMedium,
                color = InkBlack,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Выберите список или создайте новый",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InkRed)
                }

                state.lists.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "У вас пока нет списков",
                        color = InkMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(state.lists, key = { it.id }) { list ->
                        ListPickerRow(
                            list = list,
                            isAdding = state.addingToListId == list.id,
                            onClick = {
                                viewModel.addWordToList(list.id, wordId) {
                                    scope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(InkDivider),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка "Создать новый"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showCreateDialog = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(InkRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Создать новый список",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkBlack,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        SnackbarHost(snackbarHostState)
    }

    if (showCreateDialog) {
        CreateListDialog(
            onConfirm = { title ->
                viewModel.createListAndAdd(title, wordId) {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun ListPickerRow(
    list: UserList,
    isAdding: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isAdding, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PorcelainBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = list.icon ?: "📋", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = list.title,
                style = MaterialTheme.typography.bodyLarge,
                color = InkBlack,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${list.wordCount} слов",
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
            )
        }
        if (isAdding) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = InkRed,
                strokeWidth = 2.dp,
            )
        }
    }
}
