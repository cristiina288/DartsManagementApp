package org.darts.dartsmanagement.data.bars.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveBarRequest(
    val name: String,
    val description: String?,
    val machineId: Int?,
    val locationId: Int?,
    val locationBarUrl: String?
)