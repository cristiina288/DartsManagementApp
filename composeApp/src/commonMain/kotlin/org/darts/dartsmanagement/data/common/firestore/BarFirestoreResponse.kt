package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable

@Serializable
data class BarFirestoreResponse(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val license_id: String = "",
    val location: BarLocationFirestoreDetails = BarLocationFirestoreDetails(),
    val status_id: Long = 0,
    val machine_ids: List<Long> = emptyList()
)

@Serializable
data class BarLocationFirestoreDetails(
    val id: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationBarUrl: String? = null
)
