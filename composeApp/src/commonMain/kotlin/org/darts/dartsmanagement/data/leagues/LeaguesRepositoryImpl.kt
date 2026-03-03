package org.darts.dartsmanagement.data.leagues

import org.darts.dartsmanagement.domain.leagues.League
import org.darts.dartsmanagement.domain.leagues.LeaguesRepository

class LeaguesRepositoryImpl(
    private val leaguesApiService: LeaguesApiService
) : LeaguesRepository {
    override suspend fun getLeagues(): List<League> {
        return leaguesApiService.getLeagues().map {
            League(
                id = it.id,
                name = it.name,
                price = it.price
            )
        }
    }
}
