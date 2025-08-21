package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.domain.locations.model.LocationModel

interface LocationsRepository {

    suspend fun getLocations(): List<LocationModel>

}