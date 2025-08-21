package org.darts.dartsmanagement.data.machines.response

import kotlinx.serialization.Serializable


@Serializable
data class GetMachinesResponse (
    val machines: List<MachineResponse>
)