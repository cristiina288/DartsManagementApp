package org.darts.dartsmanagement.domain.leagues

import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel

interface LeaguesRepository {
    suspend fun getLeagues(): List<LeagueModel>
    suspend fun saveLeagueCollection(leagueCollection: LeagueCollectionModel): String
    suspend fun updateLeagueBarFinances(leagueId: String, barId: String, amountPaid: Double)
}
