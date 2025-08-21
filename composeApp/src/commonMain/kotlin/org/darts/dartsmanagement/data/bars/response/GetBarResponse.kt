package org.darts.dartsmanagement.data.bars.response

import kotlinx.serialization.Serializable


@Serializable
data class GetBarResponse (
    val bars: List<BarResponse>
)