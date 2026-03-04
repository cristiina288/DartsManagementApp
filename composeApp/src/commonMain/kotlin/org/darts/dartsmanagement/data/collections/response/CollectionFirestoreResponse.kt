package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CollectionFirestoreResponse(
    val id: String = "",
    val licenseId: String = "",
    val status: String = "active",
    val barId: String = "",
    val barName: String = "",
    val totalBarAmount: Double = 0.0,
    val totalBusinessAmount: Double = 0.0,
    val comments: String? = "",
    val billingMonth: String? = null,
    val recordedBy: String? = null,
    val createdAt: FirestoreTimestamp? = null,
    val machinesCollection: List<MachineCollectionFirestoreResponse> = emptyList()
)

@Serializable
data class MachineCollectionFirestoreResponse(
    val machineId: Int = 0,
    val barAmount: Double = 0.0,
    val businessAmount: Double = 0.0,
    val totalCollection: Double = 0.0
)

@Serializable
data class FirestoreTimestamp(
    val seconds: Long = 0,
    val nanoseconds: Long = 0
)