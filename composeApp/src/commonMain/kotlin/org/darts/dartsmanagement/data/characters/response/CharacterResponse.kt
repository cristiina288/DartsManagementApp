package org.darts.dartsmanagement.data.characters.remote.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.characters.model.CharacterModel


@Serializable
data class CharacterResponse (
    val id: Int,
    val status: String,
    val image: String
) {
    fun toDomain(): CharacterModel {
        return CharacterModel(
            id = id,
            isAlive = status.lowercase() == "alive",
            image = image
        )
    }
}