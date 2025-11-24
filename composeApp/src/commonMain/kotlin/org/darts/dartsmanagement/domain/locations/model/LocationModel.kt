package org.darts.dartsmanagement.domain.locations.model

import org.darts.dartsmanagement.domain.bars.models.BarModel

data class LocationModel (
    val id: String?,
    val name: String?,
    val postalCode: String?,
    val bars: List<BarModel> = emptyList()
)