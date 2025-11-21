package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

interface CollectionsRepository {

    suspend fun saveCollection(collectionAmountsModel: CollectionAmountsModel,
                               newCounterMachine: Int,
                               machineId: Int,
                               observations: String)

    suspend fun getCollectionsById(machineId: Int) : List<CollectionModel>

}