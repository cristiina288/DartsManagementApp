package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.common.model.StatusModel

@Serializable
class CollectionModel(
    val id: Int,
    val machineId: Int,
    val collectionAmounts: CollectionAmountsModel,
    val comments: String? = null,
    val status: StatusModel? = null,
    val createdAt: Long
)