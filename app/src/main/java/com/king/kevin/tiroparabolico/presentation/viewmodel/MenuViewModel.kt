package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val session: UserSession? = null,
    val studentLabs: List<Lab> = emptyList(),
    val isLoading: Boolean = false
)

class MenuViewModel(
    private val authRepository: AuthRepository,
    private val labRepository: LabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(session = authRepository.getCurrentSession()))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadStudentContent()
    }

    private fun loadStudentContent() {
        val session = uiState.value.session
        if (session?.role?.lowercase() == "student" && session.course.isNotBlank()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                labRepository.observeLabsByCourse(session.course).collect { result ->
                    result.onSuccess { labs ->
                        _uiState.update { it.copy(isLoading = false, studentLabs = labs) }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
