package org.darts.dartsmanagement.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the current user session data.
 * Currently in-memory, resets on app restart.
 */
class SessionManager {
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _licenseId = MutableStateFlow<String?>(null)
    val licenseId: StateFlow<String?> = _licenseId.asStateFlow()

    fun updateSession(name: String, license: String) {
        _userName.value = name
        _licenseId.value = license
    }

    fun clearSession() {
        _userName.value = null
        _licenseId.value = null
    }
}
