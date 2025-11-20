package org.darts.dartsmanagement.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException

import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Firebase Auth instance from KMP library
    private val auth = Firebase.auth

    // Estado interno mutable
    private val _uiState = MutableStateFlow(AuthUiState())

    // Estado público inmutable
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Verificar si hay un usuario ya autenticado
        checkCurrentUser()
    }

    /**
     * Verifica si hay un usuario autenticado actualmente
     */
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    /**
     * Actualiza el nombre del usuario
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    /**
     * Actualiza el email del usuario
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    /**
     * Actualiza la contraseña del usuario
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    /**
     * Actualiza la confirmación de contraseña
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    /**
     * Cambia entre modo login y registro
     */
    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null
        )
        clearFields()
    }

    /**
     * Limpia los campos del formulario
     */
    private fun clearFields() {
        _uiState.value = _uiState.value.copy(
            name = "",
            email = "",
            password = "",
            confirmPassword = ""
        )
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Función principal de autenticación que decide entre login o registro
     */
    internal fun authenticate() {
        if (_uiState.value.isLoginMode) {
            login()
        } else {
            register()
        }
    }

    /**
     * Realiza el login del usuario
     */
    internal fun login() {
        val currentState = _uiState.value

        // Validación básica
        if (!validateLoginFields(currentState)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            try {
                // Intentar hacer login con Firebase Auth KMP
                val result = auth.signInWithEmailAndPassword(
                    currentState.email.trim(),
                    currentState.password
                )

                // Login exitoso
                if (result.user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado durante el login"
                    )
                }

            } catch (e: Exception) {
                // Manejar errores específicos de Firebase Auth
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No existe una cuenta con este email"
                    is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos"
                    else -> "Error de conexión. Inténtalo de nuevo: ${e.message}"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    /**
     * Realiza el registro de un nuevo usuario
     */
    internal fun register() {
        val currentState = _uiState.value

        // Validación básica
        if (!validateRegisterFields(currentState)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            try {
                // Crear usuario con Firebase Auth KMP
                val result = auth.createUserWithEmailAndPassword(
                    currentState.email.trim(),
                    currentState.password
                )

                // Si el registro es exitoso, actualizar el perfil con el nombre
                result.user?.let { user ->
                    user.updateProfile(displayName = currentState.name.trim())

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado durante el registro"
                    )
                }

            } catch (e: Exception) {
                // Manejar errores específicos de Firebase Auth
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
                    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este email"
                    is FirebaseAuthInvalidCredentialsException -> "Email no válido"
                    else -> "Error de conexión. Inténtalo de nuevo: ${e.message}"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    /**
     * Valida los campos del formulario de login
     */
    private fun validateLoginFields(state: AuthUiState): Boolean {
        when {
            state.email.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "El email es obligatorio")
                return false
            }
            !isValidEmail(state.email) -> {
                _uiState.value = state.copy(errorMessage = "Email no válido")
                return false
            }
            state.password.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "La contraseña es obligatoria")
                return false
            }
        }
        return true
    }

    /**
     * Valida los campos del formulario de registro
     */
    private fun validateRegisterFields(state: AuthUiState): Boolean {
        when {
            state.name.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "El nombre es obligatorio")
                return false
            }
            state.name.length < 2 -> {
                _uiState.value = state.copy(errorMessage = "El nombre debe tener al menos 2 caracteres")
                return false
            }
            state.email.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "El email es obligatorio")
                return false
            }
            !isValidEmail(state.email) -> {
                _uiState.value = state.copy(errorMessage = "Email no válido")
                return false
            }
            state.password.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "La contraseña es obligatoria")
                return false
            }
            state.password.length < 6 -> {
                _uiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
                return false
            }
            state.confirmPassword.isBlank() -> {
                _uiState.value = state.copy(errorMessage = "Debes confirmar la contraseña")
                return false
            }
            state.password != state.confirmPassword -> {
                _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
                return false
            }
        }
        return true
    }

    /**
     * Valida el formato del email con una regex común para KMP.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return email.trim().matches(emailRegex)
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _uiState.value = AuthUiState()
        }
    }
}