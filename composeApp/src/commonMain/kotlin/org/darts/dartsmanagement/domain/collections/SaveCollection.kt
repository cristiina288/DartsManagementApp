package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class SaveCollection (val collectionsRepository: CollectionsRepository) {

    suspend operator fun invoke(
        collectionAmounts: CollectionAmountsModel,
        newCounterMachine: Int,
        machineId: Int,
        barId: String,
        comments: String,
        groupId: String,
        leaguePayment: Map<String, Any>?
    ) {
        return collectionsRepository.saveCollection(
            collectionAmounts,
            newCounterMachine,
            machineId,
            barId,
            comments,
            groupId,
            leaguePayment
        )
    }
}
