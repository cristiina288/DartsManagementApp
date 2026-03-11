package org.darts.dartsmanagement.data.bars.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveBarRequest(
    val id: String? = null,
    val name: String,
    val description: String,
    val address: String,
    val locationId: String, // This will be the ID linking to the locations collection
    val latitude: Double,
    val longitude: Double,
    val locationBarUrl: String?,
    val status: String,
    val machineIds: List<String>,
    val licenseId: String = ""
)