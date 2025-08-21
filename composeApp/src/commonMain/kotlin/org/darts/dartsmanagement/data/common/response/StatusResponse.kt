package org.darts.dartsmanagement.data.common.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.common.model.StatusModel


@Serializable
data class StatusResponse (
    val id: Int,
    //val createdOn: DateTime
    //val deletedOn: DateTime
) {
    fun toDomain(): StatusModel {
        return StatusModel(
            id = id
        )
    }
}