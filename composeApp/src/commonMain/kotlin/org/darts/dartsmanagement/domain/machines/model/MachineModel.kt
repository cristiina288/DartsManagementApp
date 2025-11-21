package org.darts.dartsmanagement.domain.machines.model

import org.darts.dartsmanagement.domain.common.model.StatusModel


data class MachineModel (
    val id: Int?,
    val name: String?,
    val counter: Int?,
    val barId: String?,
    val status: StatusModel
)