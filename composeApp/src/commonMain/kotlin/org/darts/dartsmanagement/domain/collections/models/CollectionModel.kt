package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.common.model.StatusModel

@Serializable
data class CollectionModel(
    val id: String,
    val licenseId: String,
    val status: String,
    val barId: String,
    val barName: String,
    val totalBarAmount: Double,
    val totalBusinessAmount: Double,
    val totalCollection: Double,
    val comments: String,
    val billingMonth: String,
    val recordedBy: String,
    val createdAt: Long,
    val machinesCollection: List<MachineCollectionModel>
)

@Serializable
data class MachineCollectionModel(
    val machineId: Int,
    val barAmount: Double,
    val businessAmount: Double,
    val totalCollection: Double
)
