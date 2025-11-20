/*
package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel


@Serializable
data class CollectionAmountsResponse (
    val totalCollection: Int,
    val barAmount: Int,
    val barPayment: Int,
    val businessAmount: Int,
    val extraAmount: Int
) {
    fun toDomain(): CollectionAmountsModel {
        return CollectionAmountsModel(
            totalCollection = totalCollection,
            barAmount = barAmount,
            barPayment = barPayment,
            businessAmount = businessAmount,
            extraAmount = extraAmount
        )
    }
}*/
