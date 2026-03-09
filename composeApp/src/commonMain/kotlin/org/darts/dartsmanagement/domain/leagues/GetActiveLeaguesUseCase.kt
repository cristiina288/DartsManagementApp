package org.darts.dartsmanagement.domain.leagues

import org.darts.dartsmanagement.domain.leagues.models.LeagueModel

class GetActiveLeaguesUseCase(
    private val leaguesRepository: LeaguesRepository
) {
    suspend operator fun invoke(): List<LeagueModel> {
        // Repository should already filter by licenseId
        return leaguesRepository.getLeagues().filter { it.status == "ACTIVE" }
    }
}
