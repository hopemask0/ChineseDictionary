package com.diploma.chinesedictionary.di

import com.diploma.chinesedictionary.ui.MainViewModel
import com.diploma.chinesedictionary.ui.screens.home.HomeViewModel
import com.diploma.chinesedictionary.ui.screens.lists.AddToListViewModel
import com.diploma.chinesedictionary.ui.screens.lists.ListDetailViewModel
import com.diploma.chinesedictionary.ui.screens.lists.ListsViewModel
import com.diploma.chinesedictionary.ui.screens.onboarding.OnboardingViewModel
import com.diploma.chinesedictionary.ui.screens.leveltest.LevelTestViewModel
import com.diploma.chinesedictionary.ui.screens.profile.ProfileViewModel
import com.diploma.chinesedictionary.ui.screens.search.SearchViewModel
import com.diploma.chinesedictionary.ui.screens.trainer.TrainerViewModel
import com.diploma.chinesedictionary.ui.screens.worddetail.WordDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { MainViewModel(authRepository = get()) }

    viewModel {
        HomeViewModel(
            authRepository = get(),
            dictionaryRepository = get(),
        )
    }

    viewModel { OnboardingViewModel(authRepository = get()) }
    viewModel { ProfileViewModel(authRepository = get()) }
    viewModel { SearchViewModel(repository = get()) }
    viewModel { WordDetailViewModel(repository = get(), savedStateHandle = get()) }

    // Списки (Итерация 5)
    viewModel { ListsViewModel(repository = get()) }
    viewModel { ListDetailViewModel(repository = get(), savedStateHandle = get()) }
    viewModel { AddToListViewModel(repository = get()) }

    // Тренажёр (Итерация 6)
    viewModel { TrainerViewModel(repository = get(), savedStateHandle = get()) }

    // Входной тест (Итерация 7)
    viewModel { LevelTestViewModel(repository = get()) }
}