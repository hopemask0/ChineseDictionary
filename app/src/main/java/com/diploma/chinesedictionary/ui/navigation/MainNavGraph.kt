package com.diploma.chinesedictionary.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.diploma.chinesedictionary.ui.screens.DialogScreen
import com.diploma.chinesedictionary.ui.screens.home.HomeScreen
import com.diploma.chinesedictionary.ui.screens.lists.ListDetailScreen
import com.diploma.chinesedictionary.ui.screens.lists.ListsScreen
import com.diploma.chinesedictionary.ui.screens.leveltest.LevelTestScreen
import com.diploma.chinesedictionary.ui.screens.profile.ProfileScreen
import com.diploma.chinesedictionary.ui.screens.search.SearchScreen
import com.diploma.chinesedictionary.ui.screens.trainer.TrainerListPickerScreen
import com.diploma.chinesedictionary.ui.screens.trainer.TrainerScreen
import com.diploma.chinesedictionary.ui.screens.worddetail.WordDetailScreen

/**
 * Граф основного приложения — табы Bottom Navigation + детальные экраны.
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    navigation(
        route = Routes.MainGraph.route,
        startDestination = Routes.Home.route,
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onWordOfTheDayClick = { wordId ->
                    navController.navigate(Routes.WordDetail.build(wordId))
                },
                onSearchClick = {
                    navController.navigate(Routes.Search.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onListsClick = {
                    navController.navigate(Routes.Lists.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onTrainerClick = {
                    navController.navigate(Routes.TrainerPicker.route)
                },
            )
        }

        composable(Routes.Search.route) {
            SearchScreen(
                onWordClick = { wordId ->
                    navController.navigate(Routes.WordDetail.build(wordId))
                },
            )
        }

        composable(Routes.Lists.route) {
            ListsScreen(
                onOpenList = { listId ->
                    navController.navigate(Routes.ListDetail.build(listId))
                },
            )
        }

        composable(Routes.Dialog.route) { DialogScreen() }

        composable(Routes.Profile.route) {
            ProfileScreen(
                onLogout = onLogout,
                onLevelTest = { navController.navigate(Routes.LevelTest.route) },
            )
        }

        // === Детальные экраны ===

        composable(
            route = Routes.WordDetail.route,
            arguments = listOf(
                navArgument(NavArgs.WORD_ID) { type = NavType.IntType }
            ),
        ) {
            WordDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ListDetail.route,
            arguments = listOf(
                navArgument(NavArgs.LIST_ID) { type = NavType.IntType }
            ),
        ) {
            ListDetailScreen(
                onBack = { navController.popBackStack() },
                onWordClick = { wordId ->
                    navController.navigate(Routes.WordDetail.build(wordId))
                },
                onStartTraining = { listId ->
                    navController.navigate(Routes.Trainer.build(listId))
                },
            )
        }

        // === Тренажёр (Итерация 6) ===

        composable(Routes.TrainerPicker.route) {
            TrainerListPickerScreen(
                onBack = { navController.popBackStack() },
                onStartTraining = { listId ->
                    navController.navigate(Routes.Trainer.build(listId))
                },
            )
        }

        composable(
            route = Routes.Trainer.route,
            arguments = listOf(
                navArgument(NavArgs.LIST_ID) { type = NavType.IntType }
            ),
        ) {
            TrainerScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    // После завершения возвращаемся на picker
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.LevelTest.route) {
            LevelTestScreen(
                onExit = { navController.popBackStack() },
                onCompleted = {
                    // После сохранения возвращаемся в профиль
                    navController.popBackStack()
                },
            )
        }
    }
}