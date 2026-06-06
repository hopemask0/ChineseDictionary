package com.diploma.chinesedictionary.ui.screens.lists

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.UserList
import com.diploma.chinesedictionary.domain.model.WordListTemplate
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import com.diploma.chinesedictionary.ui.theme.InkRed
import com.diploma.chinesedictionary.ui.theme.PorcelainBlue
import org.koin.androidx.compose.koinViewModel

@Composable
fun ListsScreen(
    onOpenList: (listId: Int) -> Unit,
    viewModel: ListsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Snackbar — показываем сообщения от ViewModel
    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // FAB только на вкладке "Мои" — создать новый список
            if (state.selectedTab == ListsTab.MINE) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = InkRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Создать список")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Заголовок
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "词单",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkRed,
                )
                Text(
                    text = "СПИСКИ СЛОВ",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                )
            }

            // Табы Мои/Каталог
            TabRow(
                selected = state.selectedTab,
                onTabSelected = viewModel::selectTab,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Контент
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InkRed)
                }

                state.selectedTab == ListsTab.MINE -> MyListsContent(
                    lists = state.myLists,
                    onListClick = onOpenList,
                    onShowCatalog = { viewModel.selectTab(ListsTab.CATALOG) },
                )

                state.selectedTab == ListsTab.CATALOG -> CatalogContent(
                    templates = state.catalog,
                    subscribingTemplateId = state.subscribingTemplateId,
                    subscribedSlugs = state.myLists
                        .mapNotNull { it.sourceTemplateSlug }
                        .toSet(),
                    onSubscribe = viewModel::subscribeToTemplate,
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            onConfirm = { title ->
                viewModel.createList(title, icon = null) { listId ->
                    onOpenList(listId)
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

// ============================================================================
// Табы
// ============================================================================

@Composable
private fun TabRow(
    selected: ListsTab,
    onTabSelected: (ListsTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TabChip(
            text = "Мои списки",
            selected = selected == ListsTab.MINE,
            onClick = { onTabSelected(ListsTab.MINE) },
            modifier = Modifier.weight(1f),
        )
        TabChip(
            text = "Каталог",
            selected = selected == ListsTab.CATALOG,
            onClick = { onTabSelected(ListsTab.CATALOG) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) InkRed else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, InkDivider) else null,
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else InkBlack,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ============================================================================
// Содержимое: Мои списки
// ============================================================================

@Composable
private fun MyListsContent(
    lists: List<UserList>,
    onListClick: (Int) -> Unit,
    onShowCatalog: () -> Unit,
) {
    if (lists.isEmpty()) {
        EmptyMyListsState(onShowCatalog = onShowCatalog)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(lists, key = { it.id }) { list ->
            UserListCard(
                list = list,
                onClick = { onListClick(list.id) },
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }  // место для FAB
    }
}

@Composable
private fun EmptyMyListsState(onShowCatalog: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoStories,
            contentDescription = null,
            tint = PorcelainBlue,
            modifier = Modifier.size(60.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Пока нет списков",
            style = MaterialTheme.typography.titleMedium,
            color = InkBlack,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте свой или выберите из каталога",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.clickable(onClick = onShowCatalog),
            shape = RoundedCornerShape(12.dp),
            color = PorcelainBlue,
        ) {
            Text(
                text = "Открыть каталог →",
                color = InkBlack,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun UserListCard(
    list: UserList,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Иконка слева
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PorcelainBlue),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = list.icon ?: "📋",
                    fontSize = 22.sp,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${list.wordCount} ${wordCountSuffix(list.wordCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                    if (list.isFromTemplate) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PorcelainBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "из каталога",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkBlack,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Правильное склонение для количества слов. */
private fun wordCountSuffix(count: Int): String {
    val lastTwo = count % 100
    if (lastTwo in 11..14) return "слов"
    return when (count % 10) {
        1 -> "слово"
        2, 3, 4 -> "слова"
        else -> "слов"
    }
}

// ============================================================================
// Содержимое: Каталог
// ============================================================================

@Composable
private fun CatalogContent(
    templates: List<WordListTemplate>,
    subscribingTemplateId: Int?,
    subscribedSlugs: Set<String>,
    onSubscribe: (templateId: Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(templates, key = { it.id }) { tpl ->
            TemplateCard(
                template = tpl,
                isSubscribing = subscribingTemplateId == tpl.id,
                isSubscribed = tpl.slug in subscribedSlugs,
                onSubscribe = { onSubscribe(tpl.id) },
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: WordListTemplate,
    isSubscribing: Boolean,
    isSubscribed: Boolean,
    onSubscribe: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PorcelainBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = template.icon ?: "📦", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkBlack,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${template.wordCount} ${wordCountSuffix(template.wordCount)} · ${template.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                    )
                }
            }

            if (!template.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkBlack.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка "Добавить" или метка "Уже добавлен"
            if (isSubscribed) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PorcelainBlue)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "В моих списках",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkBlack,
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.clickable(
                        enabled = !isSubscribing,
                        onClick = onSubscribe,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    color = InkRed,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isSubscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Добавляем...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "В мои списки",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
