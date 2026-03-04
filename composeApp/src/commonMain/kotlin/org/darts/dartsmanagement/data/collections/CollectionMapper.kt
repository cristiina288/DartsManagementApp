package org.darts.dartsmanagement.data.collections

import org.darts.dartsmanagement.data.collections.response.CollectionFirestoreResponse
import org.darts.dartsmanagement.domain.collections.models.CollectionModel
import org.darts.dartsmanagement.domain.common.model.StatusModel

// Modified to accept collectionId and barName to map to the new CollectionModel
fun CollectionFirestoreResponse.toDomain(collectionId: String, barName: String): CollectionModel {
    return CollectionModel(
        id = collectionId,
        licenseId = licenseId,
        status = status,
        barId = barId,
        barName = this.barName.ifBlank { barName },
        totalBarAmount = totalBarAmount,
        totalBusinessAmount = totalBusinessAmount,
        comments = comments ?: "",
        billingMonth = billingMonth ?: "",
        recordedBy = recordedBy ?: "",
        createdAt = createdAt?.seconds?.times(1000L) ?: 0L,
        machinesCollection = machinesCollection.map {
            org.darts.dartsmanagement.domain.collections.models.MachineCollectionModel(
                machineId = it.machineId,
                barAmount = it.barAmount,
                businessAmount = it.businessAmount,
                totalCollection = it.totalCollection
            )
        }
    )
}
