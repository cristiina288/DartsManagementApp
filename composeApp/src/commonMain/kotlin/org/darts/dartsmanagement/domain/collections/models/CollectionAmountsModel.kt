package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable

@Serializable
class CollectionAmountsModel (
    val totalCollection: Double,
    val barAmount: Double,
    val barPayment: Double,
    val businessAmount: Double,
    val extraAmount: Double,
)