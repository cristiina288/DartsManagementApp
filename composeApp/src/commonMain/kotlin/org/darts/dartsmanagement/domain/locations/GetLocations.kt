package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.domain.locations.model.LocationModel

class GetLocations (private val locationsRepository: LocationsRepository) {

    suspend operator fun invoke(): List<LocationModel> {
        return locationsRepository.getLocations()
    }
}
