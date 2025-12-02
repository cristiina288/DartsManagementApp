package org.darts.dartsmanagement.data.collections

import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.common.model.StatusModel

// Modified to accept collectionId and barName to map to the new CollectionModel
fun CollectionFirestoreResponse.toDomain(collectionId: String, barName: String): CollectionModel {
    return CollectionModel(
        id = collectionId,
        machineId = machineId,
        barId = barId, // New property
        barName = barName, // Added barName here
        totalCollection = totalCollection,
        barAmount = barAmount,
        businessAmount = businessAmount,
        extraAmount = extraAmount,
        comments = comments,
        status = status?.let { StatusModel(id = (it["id"] as Long).toInt()) }, // Assuming the map has an 'id' field
        createdAt = createdAt?.seconds?.times(1000L) ?: 0L
    )
}
