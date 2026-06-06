package com.diploma.chinesedictionary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.diploma.chinesedictionary.ui.AppState
import com.diploma.chinesedictionary.ui.MainViewModel
import com.diploma.chinesedictionary.ui.components.AppBottomBar
import com.diploma.chinesedictionary.ui.navigation.Routes
import com.diploma.chinesedictionary.ui.navigation.mainGraph
import com.diploma.chinesedictionary.ui.navigation.onboardingGraph
import com.diploma.chinesedictionary.ui.theme.ChineseDictionaryTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Главный Activity приложения.
 *
 * Логика "что показать":
 *   1. SPLASH      — кратко, пока MainViewModel проверяет авторизацию
 *   2. ONBOARDING  — мастер регистрации
 *   3. MAIN        — основное приложение с табами
 *
 * Переход между ONBOARDING и MAIN происходит через колбэки:
 *   - При успешной регистрации онбординг вызывает onOnboardingComplete()
 *   - При нажатии "Выйти" в профиле — onLogout()
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChineseDictionaryTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val mainViewModel: MainViewModel = koinViewModel()
    val state by mainViewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (state.state) {
            AppState.SPLASH -> SplashContent()
            AppState.ONBOARDING -> OnboardingContent(
                onFinished = { mainViewModel.onOnboardingComplete() }
            )
            AppState.MAIN -> MainContent(
                onLogout = { mainViewModel.onLogout() }
            )
        }
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Подграф онбординга со своим маленьким NavController.
 * Когда пользователь завершает онбординг, мы НЕ навигируем — мы меняем
 * глобальный AppState через mainViewModel, и Compose автоматически
 * перерисует AppRoot, переключив на MainContent.
 */
@Composable
private fun OnboardingContent(onFinished: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.OnboardingGraph.route,
    ) {
        onboardingGraph(
            navController = navController,
            onFinished = onFinished,
        )
    }
}

/**
 * Основной граф с табами и карточкой слова.
 *
 * Здесь мы используем Scaffold с Bottom Bar, потому что табы должны быть
 * видны на всех экранах основного приложения.
 *
 * Обрати внимание: Bottom Bar и NavController на этом уровне общие для
 * всех экранов внутри MainGraph — это позволяет переключаться между
 * табами без перезапуска ViewModel'ей.
 */
@Composable
private fun MainContent(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Routes.MainGraph.route,
            ) {
                mainGraph(
                    navController = navController,
                    onLogout = onLogout,
                )
            }
        }
    }
}