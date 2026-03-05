package org.darts.dartsmanagement.domain.leagues

import org.darts.dartsmanagement.domain.leagues.models.LeagueModel

class GetPendingLeaguesForBarUseCase(
    private val leaguesRepository: LeaguesRepository
) {
    suspend operator fun invoke(barId: String): List<LeagueModel> {
        val allLeagues = leaguesRepository.getLeagues()
        return allLeagues.filter { league ->
            league.ownerPayment == "BAR" && 
            league.bars.any { bar -> bar.barId == barId && bar.barFinances.paymentStatus == "PENDING" }
        }
    }
}
