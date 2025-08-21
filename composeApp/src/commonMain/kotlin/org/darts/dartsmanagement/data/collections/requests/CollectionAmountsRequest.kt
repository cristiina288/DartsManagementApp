package org.darts.dartsmanagement.data.collections.requests

import kotlinx.serialization.Serializable


@Serializable
class CollectionAmountsRequest (
    val totalCollection: Int,
    val barAmount: Int,
    val barPayment: Int,
    val businessAmount: Int,
    val extraAmount: Int,
)