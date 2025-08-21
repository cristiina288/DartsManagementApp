package org.darts.dartsmanagement.domain.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest

class SaveBar (val barRepository: BarsRepository) {

    suspend operator fun invoke(saveBarRequest: SaveBarRequest) {
        return barRepository.saveBar(saveBarRequest)
    }
}
