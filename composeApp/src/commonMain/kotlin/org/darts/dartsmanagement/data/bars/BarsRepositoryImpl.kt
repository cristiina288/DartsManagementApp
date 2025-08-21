package org.darts.dartsmanagement.data.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.BarsRepository
import org.darts.dartsmanagement.domain.bars.models.BarModel

class BarsRepositoryImpl(private val api: BarsApiService): BarsRepository {

    override suspend fun getBars(): List<BarModel> {
        return api.getBars().map { m -> m.toDomain() }
    }

    override suspend fun saveBar(saveBarRequest: SaveBarRequest) {
        return api.saveBar(saveBarRequest)
    }
}