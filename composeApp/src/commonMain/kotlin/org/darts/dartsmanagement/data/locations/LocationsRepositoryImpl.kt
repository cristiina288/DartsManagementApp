package org.darts.dartsmanagement.data.locations

import org.darts.dartsmanagement.domain.locations.LocationsRepository
import org.darts.dartsmanagement.domain.locations.model.LocationModel

class LocationsRepositoryImpl(private val api: LocationsApiService): LocationsRepository {

    override suspend fun getLocations(): List<LocationModel> {
        return api.getLocations().map { m -> m.toDomain() }
    }
}