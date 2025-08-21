package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class GetCollectionsByMachineId (private val collectionsRepository: CollectionsRepository) {

    suspend operator fun invoke(machineId: Int): List<CollectionModel> {
        return collectionsRepository.getCollectionsById(machineId)
    }
}
