package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class SaveCollection (val collectionsRepository: CollectionsRepository) {//machineName: MachineName) {

    suspend operator fun invoke(collectionAmounts: CollectionAmountsModel,
                                newCounterMachine: Int,
                                machineId: Int) {
        return collectionsRepository.saveCollection(collectionAmounts,
            newCounterMachine, machineId)
    }
}
