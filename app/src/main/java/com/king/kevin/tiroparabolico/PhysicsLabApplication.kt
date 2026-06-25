package com.king.kevin.tiroparabolico

import android.app.Application
import com.king.kevin.tiroparabolico.data.remote.AcademicRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.AuthRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.AuthResponseParser
import com.king.kevin.tiroparabolico.data.remote.ExperimentRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.JwtParser
import com.king.kevin.tiroparabolico.data.repository.AcademicRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.AuthRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.data.repository.ExperimentRepositoryImpl
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import com.king.kevin.tiroparabolico.domain.usecases.CalculateProjectileExperimentUseCase
import com.king.kevin.tiroparabolico.domain.usecases.GetCurrentSessionUseCase
import com.king.kevin.tiroparabolico.domain.usecases.LoginUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ObserveExperimentsUseCase
import com.king.kevin.tiroparabolico.domain.usecases.RegisterUseCase
import com.king.kevin.tiroparabolico.domain.usecases.SaveAcademicResponseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.SaveExperimentUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateAuthInputUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateExperimentInputUseCase
import com.king.kevin.tiroparabolico.presentation.viewmodel.AcademicViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.AuthViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.ExperimentViewModel

class PhysicsLabApplication : Application() {

    private val authRemoteDataSource by lazy { AuthRemoteDataSource() }
    private val experimentRemoteDataSource by lazy { ExperimentRemoteDataSource(this, authSessionStorage) }
    private val academicRemoteDataSource by lazy { AcademicRemoteDataSource(this, authSessionStorage) }
    private val authResponseParser by lazy { AuthResponseParser() }
    private val jwtParser by lazy { JwtParser() }
    private val authSessionStorage by lazy { AuthSessionStorage(this) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authRemoteDataSource, authResponseParser, jwtParser, authSessionStorage)
    }
    val experimentRepository: ExperimentRepository by lazy {
        ExperimentRepositoryImpl(experimentRemoteDataSource)
    }
    val academicRepository: AcademicRepository by lazy {
        AcademicRepositoryImpl(academicRemoteDataSource)
    }

    private val validateAuthInputUseCase by lazy { ValidateAuthInputUseCase() }
    private val validateExperimentInputUseCase by lazy { ValidateExperimentInputUseCase() }

    fun createAuthViewModel() = AuthViewModel(
        loginUseCase = LoginUseCase(authRepository, validateAuthInputUseCase),
        registerUseCase = RegisterUseCase(authRepository, validateAuthInputUseCase),
        getCurrentSession = GetCurrentSessionUseCase(authRepository)
    )

    fun createExperimentViewModel() = ExperimentViewModel(
        calculateProjectileExperiment = CalculateProjectileExperimentUseCase(validateExperimentInputUseCase),
        saveExperiment = SaveExperimentUseCase(experimentRepository),
        observeExperiments = ObserveExperimentsUseCase(experimentRepository)
    )

    fun createAcademicViewModel() = AcademicViewModel(
        saveAcademicResponseUseCase = SaveAcademicResponseUseCase(academicRepository)
    )
}
