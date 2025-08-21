package org.darts.dartsmanagement.data.collections.requests

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse


@Serializable
class SaveCollectionRequest(
    val collection: CollectionRequest,
    val counterMachine: Int
)

@Serializable
class CollectionRequest(
    val id: Int,
    val machineId: Int,
    val collectionAmounts: CollectionAmountsRequest,
    val comments: String? = null,
    val status: StatusResponse? = null
)