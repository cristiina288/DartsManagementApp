package org.darts.dartsmanagement.domain.collections

import kotlinx.datetime.LocalDate
import org.darts.dartsmanagement.domain.collections.models.CollectionModel

class GetCollectionsInDateRange(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<CollectionModel> {
        return collectionsRepository.getCollectionsInDateRange(startDate, endDate)
    }
}