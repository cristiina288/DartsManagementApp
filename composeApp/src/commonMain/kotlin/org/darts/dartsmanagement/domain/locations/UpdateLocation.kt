package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest

class UpdateLocation(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String, saveLocationRequest: SaveLocationRequest): Result<Unit> {
        return locationsRepository.updateLocation(locationId, saveLocationRequest)
    }
}
