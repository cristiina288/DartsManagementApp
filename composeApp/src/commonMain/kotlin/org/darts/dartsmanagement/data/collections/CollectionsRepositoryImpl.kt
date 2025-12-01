package org.darts.dartsmanagement.data.collections

import CollectionsApiService
import org.darts.dartsmanagement.domain.collections.CollectionsRepository
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class CollectionsRepositoryImpl(private val api: CollectionsApiService): CollectionsRepository {

    override suspend fun saveCollection(
        collectionAmountsModel: CollectionAmountsModel,
        newCounterMachine: Int,
        machineId: Int,
        comments: String
    ) {
        api.saveCollection(collectionAmountsModel, newCounterMachine, machineId, comments)
    }


    override suspend fun getCollectionsById(
        machineId: Int
    ) : List<CollectionModel> {
        return api.getCollectionsByMachineId(machineId).map { m -> m.toDomain() }
    }

    override suspend fun getCollectionsForMonth(year: Int, month: Int): List<CollectionModel> {
        return api.getCollectionsForMonth(year, month).map { it.toDomain() }
    }

    override suspend fun getCollectionsInDateRange(startDate: kotlinx.datetime.LocalDate, endDate: kotlinx.datetime.LocalDate): List<CollectionModel> {
        return api.getCollectionsInDateRange(startDate, endDate).map { it.toDomain() }
    }
}