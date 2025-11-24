package org.darts.dartsmanagement.data.collections

import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.common.model.StatusModel

fun CollectionFirestoreResponse.toDomain(): CollectionModel {
    return CollectionModel(
        id = 0, // Firestore documents don't have a separate ID field in the data
        machineId = machineId,
        collectionAmounts = CollectionAmountsModel(
            totalCollection = totalCollection,
            barAmount = barAmount,
            barPayment = barPayment,
            businessAmount = businessAmount,
            extraAmount = extraAmount
        ),
        comments = comments,
        status = status?.let { StatusModel(id = (it["id"] as Long).toInt()) }, // Assuming the map has an 'id' field
        createdAt = createdAt?.seconds ?: 0L
    )
}
