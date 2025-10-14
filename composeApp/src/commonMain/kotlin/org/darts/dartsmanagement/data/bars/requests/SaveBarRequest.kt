package org.darts.dartsmanagement.data.bars.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveBarRequest(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val machineId: Int?,
    val locationId: Int?,
    val locationBarUrl: String?
)