package com.diploma.chinesedictionary.ui.navigation

/**
 * Маршруты навигации приложения.
 *
 * Организованы в три группы:
 *   - Onboarding: 3 шага мастера регистрации
 *   - Main tabs: 5 табов основного приложения
 *   - Detail: дополнительные экраны (карточка слова)
 */
sealed class Routes(val route: String) {

    // === Onboarding (граф из 3 экранов) ===
    object OnboardingGraph : Routes("onboarding_graph")
    object OnboardingName : Routes("onboarding/name")
    object OnboardingGoals : Routes("onboarding/goals")
    object OnboardingLevel : Routes("onboarding/level")

    // === Основное приложение (граф с табами) ===
    object MainGraph : Routes("main_graph")
    object Home : Routes("home")
    object Search : Routes("search")
    object Lists : Routes("lists")
    object Dialog : Routes("dialog")
    object Profile : Routes("profile")

    // === Экраны с параметрами ===
    object WordDetail : Routes("worddetail/{${NavArgs.WORD_ID}}") {
        fun build(wordId: Int) = "worddetail/$wordId"
    }

    object ListDetail : Routes("listdetail/{${NavArgs.LIST_ID}}") {
        fun build(listId: Int) = "listdetail/$listId"
    }

    /** Экран выбора списка для тренировки (вход из главного экрана). */
    object TrainerPicker : Routes("trainer/picker")

    /** Сам экран тренировки по конкретному списку. */
    object Trainer : Routes("trainer/run/{${NavArgs.LIST_ID}}") {
        fun build(listId: Int) = "trainer/run/$listId"
    }

    /** Экран входного HSK-теста (без параметров — состояние внутри ViewModel). */
    object LevelTest : Routes("leveltest")
}

object NavArgs {
    const val WORD_ID = "wordId"
    const val LIST_ID = "listId"
}