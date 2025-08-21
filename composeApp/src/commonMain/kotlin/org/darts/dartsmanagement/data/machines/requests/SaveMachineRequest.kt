package org.darts.dartsmanagement.data.machines.requests

import kotlinx.serialization.Serializable

@Serializable
class SaveMachineRequest(
    val name: String,
    val counter: Int? = 0,
    val barId: Int,
)