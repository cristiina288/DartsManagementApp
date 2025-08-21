package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.models.BarModel

interface BarsRepository {

    suspend fun getBars(): List<BarModel>

    suspend fun saveBar(saveBarRequest: SaveBarRequest)

}