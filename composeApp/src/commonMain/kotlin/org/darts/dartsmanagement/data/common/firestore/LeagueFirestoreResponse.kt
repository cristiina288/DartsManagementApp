package org.darts.dartsmanagement.data.common.firestore

import kotlinx.serialization.Serializable

@Serializable
data class LeagueFirestoreResponse(
    val id: String? = null,
    val name: String = "",
    val price: String = "",
    val license_id: String = ""
)
