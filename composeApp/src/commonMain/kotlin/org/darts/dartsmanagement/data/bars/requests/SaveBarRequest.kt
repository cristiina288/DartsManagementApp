package org.darts.dartsmanagement.data.bars.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveBarRequest(
    val id: Long = 0,
    val name: String,
    val description: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val statusId: Long,
    val machineIds: List<Long>
)