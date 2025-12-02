package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.common.model.StatusModel

@Serializable
class CollectionModel(
    val id: String,
    val machineId: Int,
    val barId: String, // New property
    val barName: String, // Re-added barName
    val totalCollection: Double,
    val barAmount: Double,
    val businessAmount: Double,
    val extraAmount: Double,
    val comments: String? = null,
    val status: StatusModel? = null,
    val createdAt: Long
)
