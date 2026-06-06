package com.diploma.chinesedictionary.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.chinesedictionary.domain.model.Goal
import com.diploma.chinesedictionary.domain.model.Level
import com.diploma.chinesedictionary.domain.model.User
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeBg
import com.diploma.chinesedictionary.ui.theme.CategoryBadgeText
import com.diploma.chinesedictionary.ui.theme.InkBlack
import com.diploma.chinesedictionary.ui.theme.InkDivider
import com.diploma.chinesedictionary.ui.theme.InkMuted
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onLevelTest: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Реагируем на флаг logout
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                error = state.error!!,
                onRetry = viewModel::retry,
                onLogout = viewModel::logout,
            )
            state.user != null -> ProfileContent(
                user = state.user!!,
                onLogout = viewModel::logout,
                onLevelTest = onLevelTest,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Экран ошибки.
 * ВАЖНО: даже при ошибке загрузки даём пользователю выйти и пройти онбординг
 * заново. Иначе он может застрять, если локальный user_id не соответствует
 * данным на сервере.
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Повторить")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Выйти из аккаунта", color = InkMuted)
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    onLogout: () -> Unit,
    onLevelTest: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
    ) {
        item { SectionHeader() }
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item { HeroBlock(user = user) }
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionTitle(chineseTitle = "水平", russianTitle = "УРОВЕНЬ")
            Spacer(modifier = Modifier.height(10.dp))
            LevelBlock(level = user.level)
            Spacer(modifier = Modifier.height(10.dp))

            // Кнопка "Определить уровень" — запуск входного HSK-теста
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLevelTest),
                shape = RoundedCornerShape(12.dp),
                color = com.diploma.chinesedictionary.ui.theme.PorcelainBlue,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "🎯", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Определить уровень",
                            style = MaterialTheme.typography.bodyLarge,
                            color = com.diploma.chinesedictionary.ui.theme.InkBlack,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                        Text(
                            text = "Пройти адаптивный HSK-тест",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.diploma.chinesedictionary.ui.theme.InkMuted,
                        )
                    }
                    Text(
                        text = "→",
                        color = com.diploma.chinesedictionary.ui.theme.InkRed,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (user.goals.isNotEmpty()) {
            item {
                SectionTitle(chineseTitle = "目标", russianTitle = "ЦЕЛИ")
                Spacer(modifier = Modifier.height(10.dp))
                GoalsBlock(goals = user.goals)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader() {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "个人",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "ПРОФИЛЬ",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted,
        )
    }
}

@Composable
private fun HeroBlock(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
    ) {
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            color = InkBlack,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
        )
    }
}

@Composable
private fun LevelBlock(level: Int?) {
    val levelEnum = Level.fromCode(level)
    val displayText = when {
        levelEnum != null -> levelEnum.displayName +
                (levelEnum.hskRange?.let { " · $it" } ?: "")
        else -> "Уровень не определён — пройди тест"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun GoalsBlock(goals: List<Goal>) {
    Column {
        goals.forEach { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Badge(
                    text = goal.displayName,
                    bgColor = CategoryBadgeBg,
                    textColor = CategoryBadgeText,
                )
            }
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
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}