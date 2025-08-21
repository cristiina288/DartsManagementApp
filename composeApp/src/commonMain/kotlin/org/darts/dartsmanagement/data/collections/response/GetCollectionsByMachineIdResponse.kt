package org.darts.dartsmanagement.data.collections.response

import kotlinx.serialization.Serializable


@Serializable
data class GetCollectionsByMachineIdResponse (
    val collections: List<CollectionResponse>
)