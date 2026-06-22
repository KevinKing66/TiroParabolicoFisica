package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.usecases.GetCurrentSessionUseCase
import com.king.kevin.tiroparabolico.domain.usecases.LoginUseCase
import com.king.kevin.tiroparabolico.domain.usecases.RegisterUseCase
import com.king.kevin.tiroparabolico.presentation.state.AuthMode
import com.king.kevin.tiroparabolico.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentSession: GetCurrentSessionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState(session = getCurrentSession()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null, successMessage = null) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            loginUseCase(LoginInput(email = email.trim(), password = password))
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = session,
                            successMessage = "Bienvenido, ${session.fullName.ifBlank { "usuario" }}."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "No fue posible iniciar sesion.")
                    }
                }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        institutionName: String,
        courseCode: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            registerUseCase(
                RegisterInput(
                    fullName = fullName.trim(),
                    email = email.trim(),
                    password = password,
                    institutionName = institutionName.trim(),
                    courseCode = courseCode.trim().takeIf { it.isNotBlank() }
                )
            )
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = session,
                            mode = if (session == null) AuthMode.LOGIN else it.mode,
                            successMessage = if (session == null) {
                                "Registro creado. Ahora inicia sesion."
                            } else {
                                "Registro completado correctamente."
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "No fue posible registrar el usuario.")
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
