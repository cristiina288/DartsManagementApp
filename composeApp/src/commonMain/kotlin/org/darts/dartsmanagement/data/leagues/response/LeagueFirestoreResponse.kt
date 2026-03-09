package org.darts.dartsmanagement.data.leagues.response

import kotlinx.serialization.Serializable
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueBarModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueBarFinancesModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueTeamModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueTeamFinancesModel

@Serializable
data class LeagueFirestoreResponse(
    val id: String = "",
    val licenseId: String = "",
    val name: String = "",
    val ownerPayment: String = "",
    val price: Double = 0.0,
    val priceType: String = "",
    val status: String = "",
    val bars: List<LeagueBarFirestoreResponse> = emptyList()
) {
    fun toDomain(): LeagueModel {
        return LeagueModel(
            id = id,
            licenseId = licenseId,
            name = name,
            ownerPayment = ownerPayment,
            price = price,
            priceType = priceType,
            status = status,
            bars = bars.map { it.toDomain() }
        )
    }
}

@Serializable
data class LeagueBarFirestoreResponse(
    val barId: String = "",
    val barFinances: LeagueBarFinancesFirestoreResponse = LeagueBarFinancesFirestoreResponse(),
    val teams: List<LeagueTeamFirestoreResponse> = emptyList()
) {
    fun toDomain(): LeagueBarModel {
        return LeagueBarModel(
            barId = barId,
            barFinances = barFinances.toDomain(),
            teams = teams.map { it.toDomain() }
        )
    }
}

@Serializable
data class LeagueBarFinancesFirestoreResponse(
    val totalAmountToPay: Double = 0.0,
    val amountPaid: Double = 0.0,
    val amountPending: Double = 0.0,
    val paymentStatus: String = ""
) {
    fun toDomain(): LeagueBarFinancesModel {
        return LeagueBarFinancesModel(
            totalAmountToPay = totalAmountToPay,
            amountPaid = amountPaid,
            amountPending = amountPending,
            paymentStatus = paymentStatus
        )
    }
}

@Serializable
data class LeagueTeamFirestoreResponse(
    val teamId: String = "",
    val teamName: String = "",
    val players: Int? = null,
    val teamFinances: LeagueTeamFinancesFirestoreResponse = LeagueTeamFinancesFirestoreResponse()
) {
    fun toDomain(): LeagueTeamModel {
        return LeagueTeamModel(
            teamId = teamId,
            teamName = teamName,
            players = players,
            teamFinances = teamFinances.toDomain()
        )
    }
}

@Serializable
data class LeagueTeamFinancesFirestoreResponse(
    val totalAmountToPay: Double = 0.0,
    val amountPaid: Double = 0.0,
    val amountPending: Double = 0.0,
    val paymentStatus: String = ""
) {
    fun toDomain(): LeagueTeamFinancesModel {
        return LeagueTeamFinancesModel(
            totalAmountToPay = totalAmountToPay,
            amountPaid = amountPaid,
            amountPending = amountPending,
            paymentStatus = paymentStatus
        )
    }
}
