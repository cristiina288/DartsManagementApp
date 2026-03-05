package org.darts.dartsmanagement.data.leagues.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.collections.response.FirestoreTimestamp
import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel

@Serializable
data class LeagueCollectionFirestoreResponse(
    val id: String = "",
    val licenseId: String = "",
    val status: String = "ACTIVE",
    val leagueId: String = "",
    val amount: Double = 0.0,
    val collectionId: String = "",
    val payeeId: String = "",
    val method: String = "",
    val createdAt: FirestoreTimestamp? = null,
    val recordedBy: String = ""
) {
    fun toDomain(): LeagueCollectionModel {
        return LeagueCollectionModel(
            id = id,
            licenseId = licenseId,
            status = status,
            leagueId = leagueId,
            amount = amount,
            collectionId = collectionId,
            payeeId = payeeId,
            method = method,
            createdAt = createdAt?.seconds?.times(1000L) ?: 0L,
            recordedBy = recordedBy
        )
    }
}
