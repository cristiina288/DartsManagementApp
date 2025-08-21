package org.darts.dartsmanagement.domain.characters

import org.darts.dartsmanagement.domain.characters.model.CharacterModel

class GetRandomCharacter (val repository: Repository) {
    suspend operator fun invoke(): CharacterModel {
        val random: Int = (1..826).random()
        return repository.getSingleCharacter(random.toString())
    }
}