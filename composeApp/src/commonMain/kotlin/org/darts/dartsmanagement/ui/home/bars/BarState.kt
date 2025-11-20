package org.darts.dartsmanagement.ui.home.bars

data class BarState (
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    val machineId: Int? = null,
    val locationId: String? = null,
    val locationBarUrl: String? = null
)