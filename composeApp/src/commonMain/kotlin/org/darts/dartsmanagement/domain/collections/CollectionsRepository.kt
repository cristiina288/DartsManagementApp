package org.darts.dartsmanagement.domain.collections

import kotlinx.datetime.LocalDate
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

interface CollectionsRepository {

    suspend fun saveCollection(
        barId: String,
        barName: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        machines: List<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>,
        machineCounters: Map<String, Int>
    ): Boolean


    suspend fun getCollectionsById(machineId: Int) : List<CollectionModel>

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel>

    suspend fun getCollectionsInDateRange(startDate: LocalDate, endDate: LocalDate): List<CollectionModel>

    suspend fun getPaginatedCollections(lastCollectionCreatedAtLong: Long?, lastCollectionDocumentId: String?, limit: Int): List<CollectionModel>

    suspend fun deleteCollection(collectionId: String): Result<Unit>

    suspend fun updateCollection(collectionId: String, collectionAmountsModel: CollectionAmountsModel, comments: String): Result<Unit>
}
