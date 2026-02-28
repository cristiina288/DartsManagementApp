package org.darts.dartsmanagement.domain.collections

class DeleteCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(collectionId: String): Result<Unit> {
        return collectionsRepository.deleteCollection(collectionId)
    }
}
