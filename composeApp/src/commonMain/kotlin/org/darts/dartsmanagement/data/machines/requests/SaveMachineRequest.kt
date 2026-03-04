package org.darts.dartsmanagement.data.machines.requests

import kotlinx.serialization.Serializable

@Serializable
data class SaveMachineRequest(
    val name: String,
    val counter: Int? = null,
    val barId: String? = null,
    val status: String = "inactive" // Default status 'inactive' if no barId is provided
)