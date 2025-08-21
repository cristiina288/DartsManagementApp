package org.darts.dartsmanagement.ui.home.bars

data class BarState (
    val name: String? = null,
    val description: String = "",
    val machineId: Int? = null,
    val locationId: Int? = null,
    val locationBarUrl: String? = null
)