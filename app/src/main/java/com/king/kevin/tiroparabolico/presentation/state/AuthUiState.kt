package com.king.kevin.tiroparabolico.presentation.state

import com.king.kevin.tiroparabolico.domain.model.UserSession

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
    val session: UserSession? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

enum class AuthMode {
    LOGIN,
    REGISTER
}
