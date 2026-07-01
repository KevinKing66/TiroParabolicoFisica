package com.king.kevin.tiroparabolico

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.data.remote.AcademicRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.AuthRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.ExperimentRemoteDataSource
import com.king.kevin.tiroparabolico.data.repository.AcademicRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.AuthRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.data.repository.CourseRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.ExperimentRepositoryImpl
import com.king.kevin.tiroparabolico.data.repository.LabRepositoryImpl
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import com.king.kevin.tiroparabolico.domain.usecases.AddLabToCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.AssignStudentToCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.RemoveStudentFromCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.CalculateProjectileExperimentUseCase
import com.king.kevin.tiroparabolico.domain.usecases.CreateCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.GetCurrentSessionUseCase
import com.king.kevin.tiroparabolico.domain.usecases.GetCurrentUserCodeUseCase
import com.king.kevin.tiroparabolico.domain.usecases.LoginUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ObserveExperimentsUseCase
import com.king.kevin.tiroparabolico.domain.usecases.RegisterUseCase
import com.king.kevin.tiroparabolico.domain.usecases.SaveAcademicResponseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.SaveExperimentUseCase
import com.king.kevin.tiroparabolico.domain.usecases.UpdateCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateAuthInputUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateExperimentInputUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateRoleUseCase
import com.king.kevin.tiroparabolico.presentation.viewmodel.AcademicViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.AssignmentViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.AuthViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.CourseViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.ExperimentViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.MenuViewModel
import com.king.kevin.tiroparabolico.presentation.viewmodel.AdminViewModel
import com.king.kevin.tiroparabolico.data.repository.InstitutionRepositoryImpl
import com.king.kevin.tiroparabolico.domain.repository.InstitutionRepository
import com.king.kevin.tiroparabolico.domain.usecases.CreateUserByAdminUseCase

class PhysicsLabApplication : Application() {

    private val authRemoteDataSource by lazy { AuthRemoteDataSource(firestore) }
    private val experimentRemoteDataSource by lazy { ExperimentRemoteDataSource(this, authSessionStorage) }
    private val academicRemoteDataSource by lazy { AcademicRemoteDataSource(this, authSessionStorage) }
    private val authSessionStorage by lazy { AuthSessionStorage(this) }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authRemoteDataSource, authSessionStorage)
    }
    val experimentRepository: ExperimentRepository by lazy {
        ExperimentRepositoryImpl(experimentRemoteDataSource)
    }
    val academicRepository: AcademicRepository by lazy {
        AcademicRepositoryImpl(academicRemoteDataSource)
    }
    val courseRepository: CourseRepository by lazy {
        CourseRepositoryImpl(firestore)
    }
    val labRepository: LabRepository by lazy {
        LabRepositoryImpl(firestore)
    }
    val institutionRepository: InstitutionRepository by lazy {
        InstitutionRepositoryImpl(firestore)
    }

    private val validateAuthInputUseCase by lazy { ValidateAuthInputUseCase() }
    private val validateExperimentInputUseCase by lazy { ValidateExperimentInputUseCase() }
    private val validateRoleUseCase by lazy { ValidateRoleUseCase(authRepository) }
    private val getCurrentUserCodeUseCase by lazy { GetCurrentUserCodeUseCase(authRepository) }
    private val createUserByAdminUseCase by lazy { CreateUserByAdminUseCase(authRepository, validateRoleUseCase) }

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

    fun createMenuViewModel() = MenuViewModel(
        authRepository = authRepository,
        labRepository = labRepository
    )

    fun createCourseViewModel() = CourseViewModel(
        courseRepository = courseRepository,
        authRepository = authRepository,
        createCourse = CreateCourseUseCase(courseRepository, validateRoleUseCase),
        updateCourse = UpdateCourseUseCase(courseRepository, labRepository, validateRoleUseCase, getCurrentUserCodeUseCase),
        validateRole = validateRoleUseCase,
        getCurrentUserCode = getCurrentUserCodeUseCase
    )

    fun createAssignmentViewModel() = AssignmentViewModel(
        assignStudent = AssignStudentToCourseUseCase(courseRepository, authRepository, validateRoleUseCase),
        removeStudent = RemoveStudentFromCourseUseCase(courseRepository, authRepository, validateRoleUseCase),
        validateRole = validateRoleUseCase,
        authRepository = authRepository,
        courseRepository = courseRepository
    )

    fun createLabViewModel() = LabViewModel(
        addLab = AddLabToCourseUseCase(labRepository, courseRepository, validateRoleUseCase, getCurrentUserCodeUseCase),
        labRepository = labRepository
    )

    fun createAdminViewModel() = AdminViewModel(
        institutionRepository = institutionRepository,
        createUserByAdmin = createUserByAdminUseCase,
        validateRole = validateRoleUseCase
    )
}
