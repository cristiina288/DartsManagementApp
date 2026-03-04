package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class SaveCollection (val collectionsRepository: CollectionsRepository) {//machineName: MachineName) {

    suspend operator fun invoke(
        barId: String,
        barName: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>,
        machineCounters: Map<String, Int>
    ): Boolean {
        return collectionsRepository.saveCollection(
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
}
