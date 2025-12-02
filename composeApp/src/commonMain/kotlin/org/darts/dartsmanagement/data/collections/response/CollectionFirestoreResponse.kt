package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CollectionFirestoreResponse(
    val id: String = "",
    val machineId: Int = 0,
    val barId: String = "",
    val comments: String? = "",
    val totalCollection: Double = 0.0,
    val barAmount: Double = 0.0,
    val barPayment: Double = 0.0,
    val businessAmount: Double = 0.0,
    val extraAmount: Double = 0.0,
    val createdAt: FirestoreTimestamp? = null,
    val status: Map<String, Long>? = null
)

@Serializable
data class FirestoreTimestamp(
    val seconds: Long = 0,
    val nanoseconds: Long = 0
)