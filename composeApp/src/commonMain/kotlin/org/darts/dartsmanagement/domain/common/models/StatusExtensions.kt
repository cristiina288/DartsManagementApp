package org.darts.dartsmanagement.domain.common.models

import org.darts.dartsmanagement.domain.common.model.StatusModel

// Extension property to convert StatusModel to Status enum
val StatusModel.toStatus: Status
    get() = Status.entries.find { it.id == this.id } ?: Status.UNDEFINED

// Extension property to convert Status enum to StatusModel
val Status.toStatusModel: StatusModel
    get() = StatusModel(this.id)