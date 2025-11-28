package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.models.BarModel

interface BarsRepository {

    suspend fun getBars(): List<BarModel>

    suspend fun getBar(barId: String): Result<BarModel>

    suspend fun saveBar(saveBarRequest: SaveBarRequest)

    suspend fun updateBarMachines(barId: String, machineIds: List<Int>): Result<Unit>

}