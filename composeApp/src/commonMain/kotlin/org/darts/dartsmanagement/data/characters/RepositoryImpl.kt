package org.darts.dartsmanagement.data.characters

import org.darts.dartsmanagement.domain.characters.Repository
import org.darts.dartsmanagement.domain.characters.model.CharacterModel

class RepositoryImpl(private val api: ApiService): Repository {
    override suspend fun getSingleCharacter(id: String): CharacterModel {
        return api.getSingleCharacter(id).toDomain()
    }
}