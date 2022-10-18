package com.michaelmccormick.data.di

import com.michaelmccormick.data.repositories.StarlingRepository
import com.michaelmccormick.data.repositories.StarlingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindStarlingRepository(
        starlingRepositoryImpl: StarlingRepositoryImpl,
    ): StarlingRepository
}
