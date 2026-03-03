package org.darts.dartsmanagement.domain.leagues

interface LeaguesRepository {
    suspend fun getLeagues(): List<League>
}
