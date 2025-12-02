package org.darts.dartsmanagement.domain.locations

import org.darts.dartsmanagement.data.locations.requests.SaveLocationRequest
import org.darts.dartsmanagement.domain.locations.model.LocationModel

interface LocationsRepository {

    suspend fun getLocations(): List<LocationModel>
    suspend fun saveLocation(saveLocationRequest: SaveLocationRequest): String

}