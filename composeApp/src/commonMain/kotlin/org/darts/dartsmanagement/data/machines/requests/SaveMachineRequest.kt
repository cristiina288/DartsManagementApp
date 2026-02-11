package org.darts.dartsmanagement.data.machines.requests

import kotlinx.serialization.Serializable

@Serializable
data class SaveMachineRequest(
    val name: String,
    val counter: Int? = null,
    val barId: String? = null,
    val status: Map<String, Int> = mapOf("id" to 2) // Default status 2 if no barId is provided
)