package org.darts.dartsmanagement.data.bars.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveBarRequest(
    val id: Long = 0,
    val name: String,
    val description: String,
    val address: String,
    val locationId: String, // This will be the ID linking to the locations collection
    val latitude: Double,
    val longitude: Double,
    val locationBarUrl: String?,
    val statusId: Long,
    val machineIds: List<Long>
)