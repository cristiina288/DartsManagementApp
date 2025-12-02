package org.darts.dartsmanagement.domain.collections

import kotlinx.datetime.LocalDate
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

interface CollectionsRepository {

    suspend fun saveCollection(collectionAmountsModel: CollectionAmountsModel,
                               newCounterMachine: Int,
                               machineId: Int,
                               barId: String, // Keep barId
                               observations: String)

    suspend fun getCollectionsById(machineId: Int) : List<CollectionModel>

    suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel>

    suspend fun getCollectionsInDateRange(startDate: LocalDate, endDate: LocalDate): List<CollectionModel>

    suspend fun getPaginatedCollections(lastCollectionCreatedAtLong: Long?, lastCollectionDocumentId: String?, limit: Int): List<CollectionModel>
}
