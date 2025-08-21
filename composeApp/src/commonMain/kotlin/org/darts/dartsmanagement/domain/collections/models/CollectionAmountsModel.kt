package org.darts.dartsmanagement.domain.collections.models

import kotlinx.serialization.Serializable

@Serializable
class CollectionAmountsModel (
    val totalCollection: Int,
    val barAmount: Int,
    val barPayment: Int,
    val businessAmount: Int,
    val extraAmount: Int,
)