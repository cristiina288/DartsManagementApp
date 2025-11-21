package org.darts.dartsmanagement.domain.common.models

enum class Status(val id: Int) {
    UNDEFINED(0),
    ACTIVE(1),
    INACTIVE(2),
    PENDING_REPAIR(3)
}
