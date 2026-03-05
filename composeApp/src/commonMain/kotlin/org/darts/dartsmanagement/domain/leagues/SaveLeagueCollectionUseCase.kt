package org.darts.dartsmanagement.domain.leagues

import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel

class SaveLeagueCollectionUseCase(
    private val leaguesRepository: LeaguesRepository
) {
    suspend operator fun invoke(leagueCollection: LeagueCollectionModel): String {
        val collectionId = leaguesRepository.saveLeagueCollection(leagueCollection)
        if (collectionId.isNotEmpty()) {
            leaguesRepository.updateLeagueBarFinances(
                leagueId = leagueCollection.leagueId,
                barId = leagueCollection.payeeId,
                amountPaid = leagueCollection.amount
            )
        }
        return collectionId
    }
}
