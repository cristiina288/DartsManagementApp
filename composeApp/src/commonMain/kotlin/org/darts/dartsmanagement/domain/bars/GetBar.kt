package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.domain.bars.models.BarModel

class GetBar (val barsRepository: BarsRepository) {//machineName: MachineName) {

    suspend operator fun invoke(barId: String): Result<BarModel> {
        return barsRepository.getBar(barId)
    }
}
