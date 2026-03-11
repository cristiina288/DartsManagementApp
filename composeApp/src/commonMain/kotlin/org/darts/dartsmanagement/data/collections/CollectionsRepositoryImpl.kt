package org.darts.dartsmanagement.data.collections

import kotlinx.datetime.LocalDate
import org.darts.dartsmanagement.domain.collections.CollectionsRepository
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class CollectionsRepositoryImpl(private val api: CollectionsApiService): CollectionsRepository {

    override suspend fun saveCollection(
        barId: String,
        barName: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>,
        machineCounters: Map<String, Int>
    ): String? {
        return api.saveCollection(
            barId,
            barName,
            comments,
            totalBarAmount,
            totalBusinessAmount,
            totalCollection,
            machines,
            machineCounters
        )
    }



    override suspend fun getCollectionsById(
        machineId: String
    ) : List<CollectionModel> {
        return api.getCollectionsByMachineId(machineId)
    }

    override suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel> {
        return api.getCollectionsForMonth(year, month)
    }

    override suspend fun getCollectionsInDateRange(startDate: LocalDate, endDate: LocalDate): List<CollectionModel> {
        return api.getCollectionsInDateRange(startDate, endDate)
    }

    override suspend fun getPaginatedCollections(lastCollectionCreatedAtLong: Long?, lastCollectionDocumentId: String?, limit: Int): List<CollectionModel> {
        return api.getPaginatedCollections(lastCollectionCreatedAtLong, lastCollectionDocumentId, limit)
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return runCatching {
            api.deleteCollection(collectionId)
        }
    }

    override suspend fun updateCollection(
        collectionId: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>
    ): Result<Unit> {
        return runCatching {
            api.updateCollection(collectionId, comments, totalBarAmount, totalBusinessAmount, totalCollection, machines)
        }
    }
}