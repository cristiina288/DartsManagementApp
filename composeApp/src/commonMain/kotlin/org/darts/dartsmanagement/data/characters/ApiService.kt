package org.darts.dartsmanagement.data.characters

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.darts.dartsmanagement.data.characters.remote.response.CharacterResponse

class ApiService(private val httpClient: HttpClient) {

    suspend fun getSingleCharacter(id: String): CharacterResponse {
        return httpClient.get("/api/character/$id").body()
    }
}