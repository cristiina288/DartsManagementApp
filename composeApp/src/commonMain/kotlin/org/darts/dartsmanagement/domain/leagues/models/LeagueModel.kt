package org.darts.dartsmanagement.domain.leagues.models

import kotlinx.serialization.Serializable

@Serializable
data class LeagueModel(
    val id: String,
    val licenseId: String,
    val name: String,
    val ownerPayment: String,
    val price: Double,
    val priceType: String,
    val status: String,
    val bars: List<LeagueBarModel>
)

@Serializable
data class LeagueBarModel(
    val barId: String,
    val barFinances: LeagueBarFinancesModel,
    val teams: List<LeagueTeamModel>
)

@Serializable
data class LeagueBarFinancesModel(
    val totalAmountToPay: Double,
    val amountPaid: Double,
    val amountPending: Double,
    val paymentStatus: String
)

@Serializable
data class LeagueTeamModel(
    val teamId: String,
    val teamName: String,
    val players: Int? = null,
    val teamFinances: LeagueTeamFinancesModel
)

@Serializable
data class LeagueTeamFinancesModel(
    val totalAmountToPay: Double,
    val amountPaid: Double,
    val amountPending: Double,
    val paymentStatus: String
)
