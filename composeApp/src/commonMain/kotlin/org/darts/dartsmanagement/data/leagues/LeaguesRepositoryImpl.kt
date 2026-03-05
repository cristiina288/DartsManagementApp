package org.darts.dartsmanagement.data.leagues

import org.darts.dartsmanagement.domain.leagues.LeaguesRepository
import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel

class LeaguesRepositoryImpl(
    private val api: LeaguesApiService
) : LeaguesRepository {
    override suspend fun getLeagues(): List<LeagueModel> = api.getLeagues()
    
    override suspend fun saveLeagueCollection(leagueCollection: LeagueCollectionModel): String = 
        api.saveLeagueCollection(leagueCollection)

    override suspend fun updateLeagueFinances(leagueId: String, payeeId: String, amountPaid: Double) = 
        api.updateLeagueFinances(leagueId, payeeId, amountPaid)
}
