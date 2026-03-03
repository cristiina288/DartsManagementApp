package org.darts.dartsmanagement.data.leagues.response

import kotlinx.serialization.Serializable

@Serializable
data class LeagueResponse(
    val id: String,
    val name: String,
    val price: String
)
