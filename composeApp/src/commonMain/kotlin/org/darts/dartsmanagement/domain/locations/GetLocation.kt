package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.domain.locations.model.LocationModel

class GetLocation(
    private val locationsRepository: LocationsRepository
) {
    suspend operator fun invoke(locationId: String): Result<LocationModel> {
        return try {
            val location = locationsRepository.getLocations().find { it.id == locationId }
            if (location != null) {
                Result.success(location)
            } else {
                Result.failure(NoSuchElementException("Ubicación no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
