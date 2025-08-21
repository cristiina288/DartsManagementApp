package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.domain.bars.models.BarModel

class GetBars (val barsRepository: BarsRepository) {//machineName: MachineName) {

    suspend operator fun invoke(): List<BarModel> {
        return barsRepository.getBars()
    }
}
