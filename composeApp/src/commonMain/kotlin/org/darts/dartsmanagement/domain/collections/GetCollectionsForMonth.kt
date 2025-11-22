package org.darts.dartsmanagement.domain.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class GetCollectionsForMonth(private val collectionsRepository: CollectionsRepository) {
    suspend operator fun invoke(year: Int, month: Int): List<CollectionModel> {
        return collectionsRepository.getCollectionsForMonth(year, month)
    }
}
