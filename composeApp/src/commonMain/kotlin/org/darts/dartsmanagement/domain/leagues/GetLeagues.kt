package org.darts.dartsmanagement.domain.leagues

class GetLeagues(private val leaguesRepository: LeaguesRepository) {
    suspend operator fun invoke(): List<League> = leaguesRepository.getLeagues()
}
