package com.diploma.chinesedictionary.ui.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.diploma.chinesedictionary.ui.screens.onboarding.OnboardingGoalsScreen
import com.diploma.chinesedictionary.ui.screens.onboarding.OnboardingLevelScreen
import com.diploma.chinesedictionary.ui.screens.onboarding.OnboardingNameScreen
import com.diploma.chinesedictionary.ui.screens.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Подграф онбординга — 3 экрана с ОБЩИМ OnboardingViewModel.
 *
 * Особенность: navController.getBackStackEntry(graphRoute) возвращает
 * запись стека для всего подграфа. Передавая её как viewModelStoreOwner,
 * мы привязываем ViewModel к жизненному циклу всего подграфа, а не
 * конкретного экрана. Все три экрана получат ОДИН и тот же ViewModel.
 *
 * Когда пользователь завершает онбординг (или мы вручную навигируем
 * прочь из подграфа), ViewModel автоматически уничтожается, освобождая
 * память.
 */
fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    onFinished: () -> Unit,
) {
    navigation(
        route = Routes.OnboardingGraph.route,
        startDestination = Routes.OnboardingName.route,
    ) {
        composable(Routes.OnboardingName.route) { backStackEntry ->
            // Получаем родительскую запись стека (целый подграф онбординга)
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.OnboardingGraph.route)
            }
            // ViewModel привязан к этой родительской записи — общий для трёх экранов
            val viewModel: OnboardingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            OnboardingNameScreen(
                onNext = { navController.navigate(Routes.OnboardingGoals.route) },
                viewModel = viewModel,
            )
        }

        composable(Routes.OnboardingGoals.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.OnboardingGraph.route)
            }
            val viewModel: OnboardingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            OnboardingGoalsScreen(
                onNext = { navController.navigate(Routes.OnboardingLevel.route) },
                onBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }

        composable(Routes.OnboardingLevel.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.OnboardingGraph.route)
            }
            val viewModel: OnboardingViewModel = koinViewModel(viewModelStoreOwner = parentEntry)

            OnboardingLevelScreen(
                onBack = { navController.popBackStack() },
                onFinish = onFinished,
                viewModel = viewModel,
            )
        }
    }
}
