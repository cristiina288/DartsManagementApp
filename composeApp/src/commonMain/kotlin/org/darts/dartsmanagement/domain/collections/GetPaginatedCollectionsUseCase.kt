package org.darts.dartsmanagement.domain.collections

import kotlinx.datetime.LocalDate
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class GetPaginatedCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(lastCollectionCreatedAtLong: Long?, lastCollectionDocumentId: String?, limit: Int): List<CollectionModel> {
        return collectionsRepository.getPaginatedCollections(lastCollectionCreatedAtLong, lastCollectionDocumentId, limit)
    }
}
