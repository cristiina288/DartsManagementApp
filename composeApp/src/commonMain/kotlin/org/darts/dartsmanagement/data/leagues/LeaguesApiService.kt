package org.darts.dartsmanagement.data.leagues

import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.common.firestore.LeagueFirestoreResponse
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.leagues.response.LeagueResponse

class LeaguesApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {
    suspend fun getLeagues(): List<LeagueResponse> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val leagueDocs = firestore.getDocuments("leagues", "license_id", licenseId)
            leagueDocs.map { doc ->
                val firestoreResponse = doc.data<LeagueFirestoreResponse>()
                LeagueResponse(
                    id = doc.id,
                    name = firestoreResponse.name,
                    price = firestoreResponse.price
                )
            }
        } catch (e: Exception) {
            println("Error fetching leagues: $e")
            emptyList()
        }
    }
}
