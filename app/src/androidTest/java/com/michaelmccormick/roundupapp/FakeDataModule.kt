package com.michaelmccormick.roundupapp

import com.michaelmccormick.data.di.DataModule
import com.michaelmccormick.data.repositories.StarlingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ViewModelComponent::class],
    replaces = [DataModule::class],
)
internal abstract class FakeDataModule {
    @Binds
    internal abstract fun bindStarlingRepository(
        fakeStarlingRepositoryImpl: FakeStarlingRepositoryImpl,
    ): StarlingRepository
}
