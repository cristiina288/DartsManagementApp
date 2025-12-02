package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest

class SaveLocation(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(saveLocationRequest: SaveLocationRequest): String {
        return locationsRepository.saveLocation(saveLocationRequest)
    }
}