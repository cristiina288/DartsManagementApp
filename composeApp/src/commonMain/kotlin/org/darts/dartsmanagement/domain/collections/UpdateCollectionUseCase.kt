package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

class UpdateCollectionUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        collectionId: String,
        collectionAmountsModel: CollectionAmountsModel,
        comments: String
    ): Result<Unit> {
        return collectionsRepository.updateCollection(collectionId, collectionAmountsModel, comments)
    }
}
