
package org.darts.dartsmanagement.data.common.firestore

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class UserFirestore(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val licenseId: String = ""
)

@Serializable
data class BarFirestore(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val licenseId: String = "",
    val locationId: String = "",
    val statusId: Long = 0,
    val machineIds: List<Long> = emptyList()
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
    val lastCollection: Timestamp? = null
)

@Serializable
data class LicenseFirestore(
    val companyName: String = "",
    val expiresAt: Timestamp? = null,
    val status: String = "" // "active", "inactive", etc.
)
