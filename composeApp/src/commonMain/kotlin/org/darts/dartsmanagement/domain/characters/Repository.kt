package org.darts.dartsmanagement.domain.characters

import org.darts.dartsmanagement.domain.characters.model.CharacterModel

interface Repository {

    suspend fun getSingleCharacter(id: String): CharacterModel
}