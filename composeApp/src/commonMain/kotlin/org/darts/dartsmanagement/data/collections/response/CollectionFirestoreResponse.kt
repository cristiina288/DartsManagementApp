package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.Serializable

@Serializable
data class CollectionFirestoreResponse(
    val machineId: Int = 0,
    val comments: String = "",
    val totalCollection: Double = 0.0,
    val barAmount: Double = 0.0,
    val barPayment: Double = 0.0,
    val businessAmount: Double = 0.0,
    val extraAmount: Double = 0.0,
    // val createdAt: Timestamp? = null, // Timestamps may require special handling
    val status: Map<String, Long>? = null
)
