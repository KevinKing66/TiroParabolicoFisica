package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Institution
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.repository.InstitutionRepository
import com.king.kevin.tiroparabolico.domain.usecases.CreateUserByAdminUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateRoleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val institutions: List<Institution> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAdmin: Boolean = false
)

class AdminViewModel(
    private val institutionRepository: InstitutionRepository,
    private val createUserByAdmin: CreateUserByAdminUseCase,
    private val validateRole: ValidateRoleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        val isAdmin = validateRole(listOf("admin"))
        _uiState.update { it.copy(isAdmin = isAdmin) }
        if (isAdmin) {
            loadInstitutions()
        }
    }

    private fun loadInstitutions() {
        viewModelScope.launch {
            institutionRepository.observeAllInstitutions().collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(institutions = list) }
                }
            }
        }
    }

    fun saveInstitution(name: String, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            institutionRepository.saveInstitution(Institution(name = name, address = address))
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Institución creada") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun deleteInstitution(id: String) {
        viewModelScope.launch {
            institutionRepository.deleteInstitution(id)
        }
    }

    fun createUser(fullName: String, email: String, pass: String, inst: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val input = RegisterInput(fullName, email, pass, inst, null)
            createUserByAdmin(input, role)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Usuario $role creado") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
