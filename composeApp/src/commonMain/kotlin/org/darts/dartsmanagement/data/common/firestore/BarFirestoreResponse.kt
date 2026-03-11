package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable

@Serializable
data class BarFirestoreResponse(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val licenseId: String = "",
    val location: BarLocationFirestoreDetails = BarLocationFirestoreDetails(),
    val status: String = "ACTIVE",
    val machineIds: List<String> = emptyList()
)

@Serializable
data class BarLocationFirestoreDetails(
    val id: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationBarUrl: String? = null
)
