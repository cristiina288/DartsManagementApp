package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.domain.collections.models.CollectionModel


@Serializable
data class CollectionResponse (
    val id: Int,
    val machineId: Int,
    val collectionAmounts: CollectionAmountsResponse,
    val comments: String? = null,
    val status: StatusResponse
) {
    fun toDomain(): CollectionModel {
        return CollectionModel(
            id = id,
            machineId = machineId,
            collectionAmounts = collectionAmounts.toDomain(),
            comments = comments,
            status = status.toDomain()
        )
    }
}