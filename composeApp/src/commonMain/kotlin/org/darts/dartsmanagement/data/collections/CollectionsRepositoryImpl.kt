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
}