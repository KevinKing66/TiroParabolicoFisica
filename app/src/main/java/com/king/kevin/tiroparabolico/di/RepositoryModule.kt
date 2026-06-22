package com.king.kevin.tiroparabolico.di

import com.king.kevin.tiroparabolico.data.repository.ExperimentRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.AuthRepositoryImpl
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExperimentRepository(implementation: ExperimentRepositoryImpl): ExperimentRepository
}
