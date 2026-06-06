package com.diploma.chinesedictionary.di

import com.diploma.chinesedictionary.data.local.UserSession
import com.diploma.chinesedictionary.data.repository.AuthRepositoryImpl
import com.diploma.chinesedictionary.data.repository.DictionaryRepositoryImpl
import com.diploma.chinesedictionary.data.repository.LevelTestRepositoryImpl
import com.diploma.chinesedictionary.data.repository.ListsRepositoryImpl
import com.diploma.chinesedictionary.data.repository.ProgressRepositoryImpl
import com.diploma.chinesedictionary.domain.repository.AuthRepository
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository
import com.diploma.chinesedictionary.domain.repository.LevelTestRepository
import com.diploma.chinesedictionary.domain.repository.ListsRepository
import com.diploma.chinesedictionary.domain.repository.ProgressRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

    single<UserSession> {
        UserSession(context = androidContext())
    }

    single<DictionaryRepository> {
        DictionaryRepositoryImpl(api = get())
    } bind DictionaryRepository::class

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            userSession = get(),
        )
    } bind AuthRepository::class

    single<ListsRepository> {
        ListsRepositoryImpl(api = get())
    } bind ListsRepository::class

    single<ProgressRepository> {
        ProgressRepositoryImpl(api = get())
    } bind ProgressRepository::class

    single<LevelTestRepository> {
        LevelTestRepositoryImpl(api = get())
    } bind LevelTestRepository::class
}