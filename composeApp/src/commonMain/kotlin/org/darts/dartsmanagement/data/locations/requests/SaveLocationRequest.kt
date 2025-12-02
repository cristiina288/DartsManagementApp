package org.darts.dartsmanagement.data.locations.requests

import kotlinx.serialization.Serializable

@Serializable
data class SaveLocationRequest(
    val name: String,
    val postalCode: String
)