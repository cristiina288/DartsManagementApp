package org.darts.dartsmanagement.domain.leagues.models

import kotlinx.serialization.Serializable

@Serializable
data class LeagueCollectionModel(
    val id: String = "",
    val licenseId: String,
    val status: String = "ACTIVE",
    val leagueId: String,
    val amount: Double,
    val collectionId: String,
    val payeeId: String,
    val method: String,
    val createdAt: Long = 0,
    val recordedBy: String
)
