package org.darts.dartsmanagement.ui.locations.newLocation

data class NewLocationUiState(
    val name: String = "",
    val postalCode: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)