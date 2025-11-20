
package org.darts.dartsmanagement.data.common.firestore

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class UserFirestore(
    val email: String = "",
    val name: String = "",
    val license_id: String = ""
)

@Serializable
data class BarFirestore(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val license_id: String = "",
    val location_id: String = "",
    val status_id: Long = 0,
    val machine_ids: List<Long> = emptyList()
)

@Serializable
data class LocationFirestore(
    val address: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Serializable
data class MachineFirestore(
    val name: String = "",
    val type: String = "",
    val last_collection: Timestamp? = null
)
