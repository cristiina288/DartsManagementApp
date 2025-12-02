package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable

@Serializable
data class CollectionAmountsModel (
    val totalCollection: Double = 0.0,
    val barAmount: Double = 0.0,
    val barPayment: Double = 0.0,
    val businessAmount: Double = 0.0,
    val extraAmount: Double = 0.0,
)