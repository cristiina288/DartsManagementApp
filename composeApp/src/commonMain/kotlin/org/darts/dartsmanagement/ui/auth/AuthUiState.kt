package org.darts.dartsmanagement.ui.auth

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)