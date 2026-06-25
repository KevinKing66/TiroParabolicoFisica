package com.king.kevin.tiroparabolico.di

import android.content.Context
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

/**
 * Contenedor de inyección de dependencias sin Hilt.
 * Proporciona la creación y acceso a todas las dependencias de la aplicación.
 */
class AppContainer(private val context: Context) {
    // Remote data sources
    private val authRemoteDataSource: AuthRemoteDataSource by lazy {
        AuthRemoteDataSource()
    }

    private val experimentRemoteDataSource: ExperimentRemoteDataSource by lazy {
        ExperimentRemoteDataSource(context)
    }

    private val academicRemoteDataSource: AcademicRemoteDataSource by lazy {
        AcademicRemoteDataSource(context)
    }

    // Parsers and utilities
    private val authResponseParser: AuthResponseParser by lazy {
        AuthResponseParser()
    }

    private val jwtParser: JwtParser by lazy {
        JwtParser()
    }

    // Session storage
    private val authSessionStorage: AuthSessionStorage by lazy {
        AuthSessionStorage(context)
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            remoteDataSource = authRemoteDataSource,
            responseParser = authResponseParser,
            jwtParser = jwtParser,
            sessionStorage = authSessionStorage
        )
    }

    val experimentRepository: ExperimentRepository by lazy {
        ExperimentRepositoryImpl(remoteDataSource = experimentRemoteDataSource)
    }

    val academicRepository: AcademicRepository by lazy {
        AcademicRepositoryImpl(remoteDataSource = academicRemoteDataSource)
    }

    // Use cases
    private val validateAuthInputUseCase: ValidateAuthInputUseCase by lazy {
        ValidateAuthInputUseCase()
    }

    private val validateExperimentInputUseCase: ValidateExperimentInputUseCase by lazy {
        ValidateExperimentInputUseCase()
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(repository = authRepository, validateAuthInput = validateAuthInputUseCase)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(repository = authRepository, validateAuthInput = validateAuthInputUseCase)
    }

    val getCurrentSessionUseCase: GetCurrentSessionUseCase by lazy {
        GetCurrentSessionUseCase(repository = authRepository)
    }

    val observeExperimentsUseCase: ObserveExperimentsUseCase by lazy {
        ObserveExperimentsUseCase(repository = experimentRepository)
    }

    val saveExperimentUseCase: SaveExperimentUseCase by lazy {
        SaveExperimentUseCase(repository = experimentRepository)
    }

    val saveAcademicResponseUseCase: SaveAcademicResponseUseCase by lazy {
        SaveAcademicResponseUseCase(repository = academicRepository)
    }

    val calculateProjectileExperimentUseCase: CalculateProjectileExperimentUseCase by lazy {
        CalculateProjectileExperimentUseCase(validateExperimentInput = validateExperimentInputUseCase)
    }

    // ViewModels
    fun createAuthViewModel(): AuthViewModel = AuthViewModel(
        loginUseCase = loginUseCase,
        registerUseCase = registerUseCase,
        getCurrentSession = getCurrentSessionUseCase
    )

    fun createExperimentViewModel(): ExperimentViewModel = ExperimentViewModel(
        calculateProjectileExperiment = calculateProjectileExperimentUseCase,
        saveExperiment = saveExperimentUseCase,
        observeExperiments = observeExperimentsUseCase
    )

    fun createAcademicViewModel(): AcademicViewModel = AcademicViewModel(
        saveAcademicResponseUseCase = saveAcademicResponseUseCase
    )
}
