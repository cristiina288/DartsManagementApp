package org.darts.dartsmanagement.data.locations

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.darts.dartsmanagement.data.locations.response.GetLocationsResponse
import org.darts.dartsmanagement.data.locations.response.LocationResponse

class LocationsApiService(private val httpClient: HttpClient) {

    suspend fun getLocations(): List<LocationResponse> {

        try {
            val b: GetLocationsResponse = httpClient.get("/v1/locations").body()
            return b.locations
        } catch (e: Exception) {
            println("/locations: $e")

            try {
                val b: GetLocationsResponse = httpClient.get("/v1/locations").body()
                return b.locations
            } catch (e: Exception) {
                println("/locations: $e")
                return emptyList()
            }
        }

    }
}