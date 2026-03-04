package org.darts.dartsmanagement.domain.collections

class UpdateCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        comments: String,
        totalBarAmount: Double,
        totalBusinessAmount: Double,
        totalCollection: Double,
        machines: List<org.darts.dartsmanagement.data.collections.CollectionMachineFirestore>
    ): Result<Unit> {
        return collectionsRepository.updateCollection(
            collectionId,
            comments,
            totalBarAmount,
            totalBusinessAmount,
            totalCollection,
            machines
        )
    }
}
