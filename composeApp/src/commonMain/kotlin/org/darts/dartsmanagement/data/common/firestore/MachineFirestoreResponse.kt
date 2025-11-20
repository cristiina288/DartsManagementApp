package org.darts.dartsmanagement.data.common.firestore

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.data.common.response.StatusResponse
import org.darts.dartsmanagement.data.machines.response.MachineResponse
import org.darts.dartsmanagement.domain.machines.model.MachineModel

@Serializable
data class MachineFirestoreResponse(
    val id: String = "", // Firestore document ID
    val name: String = "",
    val type: String = "",
    val last_collection: Timestamp? = null,
    val counter: Int? = null,
    val barId: String? = null, // Assuming barId will be a String (document ID) in Firestore
    val status: StatusResponse? = null
) {
    fun toDomain(): MachineModel {
        return MachineModel(
            id = null, // Still null as MachineModel expects Int?, but we have String
            name = name,
            counter = counter,
            barId = barId?.toIntOrNull(), // Convert String barId to Int?
            status = status?.toDomain() ?: StatusResponse(id = 0).toDomain()
        )
    }

    fun toMachineResponse(): MachineResponse {
        return MachineResponse(
            id = id.toIntOrNull(), // MachineResponse expects Int?, we have String id
            name = name,
            counter = counter,
            barId = barId?.toIntOrNull(), // Convert String barId to Int?
            status = status ?: StatusResponse(id = 0) // Default status if null
        )
    }
}
