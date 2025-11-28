package org.darts.dartsmanagement.data.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.BarsRepository
import org.darts.dartsmanagement.domain.bars.models.BarModel

class BarsRepositoryImpl(private val api: BarsApiService): BarsRepository {

    override suspend fun getBars(): List<BarModel> {
        return api.getBars().map { m -> m.toDomain() }
    }

    override suspend fun getBar(barId: String): Result<BarModel> {
        return runCatching {
            api.getBar(barId).toDomain()
        }
    }

    override suspend fun saveBar(saveBarRequest: SaveBarRequest) {
       // return api.saveBar(saveBarRequest)
    }

    override suspend fun updateBarMachines(barId: String, machineIds: List<Int>): Result<Unit> {
        return runCatching {
            api.updateBarMachines(barId, machineIds)
        }
    }
}