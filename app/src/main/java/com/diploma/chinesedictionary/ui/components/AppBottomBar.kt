package com.diploma.chinesedictionary.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.diploma.chinesedictionary.ui.navigation.Routes
import com.diploma.chinesedictionary.ui.theme.InkMuted

/**
 * Bottom Navigation Bar с 5 табами.
 *
 * Согласно ТЗ диплома: Главная · Поиск · Списки · Диалог · Профиль.
 *
 * Поведение:
 *   - Текущий таб подсвечивается терракотовым цветом.
 *   - Клик по табу переключает экран и сохраняет состояние (если ушёл со
 *     списка и вернулся — список не перематывается на начало).
 *
 * Параметр currentRoute получаем из navController, чтобы знать, какой таб
 * сейчас активен. Это позволяет Compose автоматически перерисовать панель
 * при переключении табов.
 */
@Composable
fun AppBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
    ) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route.route,
                onClick = { navigateToTab(navController, tab.route.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = InkMuted,
                    unselectedTextColor = InkMuted,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
    }
}

/**
 * Перечисление табов нижней панели.
 * Каждый таб — это иконка + русский лейбл + маршрут.
 */
private enum class BottomTab(
    val route: Routes,
    val icon: ImageVector,
    val label: String,
) {
    Home(Routes.Home, Icons.Outlined.Home, "Главная"),
    Search(Routes.Search, Icons.Outlined.Search, "Поиск"),
    Lists(Routes.Lists, Icons.Outlined.Book, "Списки"),
    Dialog(Routes.Dialog, Icons.Outlined.Chat, "Диалог"),
    Profile(Routes.Profile, Icons.Outlined.Person, "Профиль"),
}

/**
 * Навигация между табами с правильным поведением back stack.
 *
 * launchSingleTop — не плодим копии одного и того же таба при многократных кликах.
 * restoreState — восстанавливаем состояние таба, если в него возвращаются.
 * popUpTo(startDestination) — стандартная схема для Bottom Navigation:
 *   все табы — корневые, между ними не накапливается история.
 */
private fun navigateToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
