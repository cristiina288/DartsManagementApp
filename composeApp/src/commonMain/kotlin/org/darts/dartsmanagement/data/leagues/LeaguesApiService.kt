package org.darts.dartsmanagement.data.leagues

import dev.gitlive.firebase.firestore.Timestamp
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.data.leagues.response.LeagueFirestoreResponse
import org.darts.dartsmanagement.domain.leagues.models.LeagueCollectionModel
import org.darts.dartsmanagement.domain.leagues.models.LeagueModel

class LeaguesApiService(
    private val firestore: ExpectedFirestore,
    private val sessionManager: SessionManager
) {
    suspend fun getLeagues(): List<LeagueModel> {
        return try {
            val licenseId = sessionManager.licenseId.value ?: return emptyList()
            val docs = firestore.getDocuments("leagues", "licenseId", licenseId)
            docs.map { it.data<LeagueFirestoreResponse>().copy(id = it.id).toDomain() }
        } catch (e: Exception) {
            println("Error fetching leagues: $e")
            emptyList()
        }
    }

    suspend fun saveLeagueCollection(leagueCollection: LeagueCollectionModel): String {
        return try {
            val data = mapOf(
                "licenseId" to leagueCollection.licenseId,
                "status" to leagueCollection.status,
                "leagueId" to leagueCollection.leagueId,
                "amount" to leagueCollection.amount,
                "collectionId" to leagueCollection.collectionId,
                "payeeId" to leagueCollection.payeeId,
                "method" to leagueCollection.method,
                "createdAt" to Timestamp.now(),
                "recordedBy" to leagueCollection.recordedBy
            )
            firestore.addDocument("leagueCollections", data)
        } catch (e: Exception) {
            println("Error saving league collection: $e")
            ""
        }
    }

    suspend fun updateLeagueFinances(leagueId: String, payeeId: String, amountPaid: Double) {
        try {
            val doc = firestore.getDocument("leagues", leagueId) ?: return
            val league = doc.data<LeagueFirestoreResponse>()

            var updated = false
            val updatedBars = league.bars.map { bar ->
                if (bar.barId == payeeId) {
                    updated = true
                    val newAmountPaid = bar.barFinances.amountPaid + amountPaid
                    val newAmountPending = (bar.barFinances.totalAmountToPay - newAmountPaid).coerceAtLeast(0.0)
                    
                    val newQuota = if (league.ownerPayment == "BAR") {
                        (bar.barFinances.quota - amountPaid).coerceAtLeast(0.0)
                    } else {
                        bar.barFinances.quota
                    }

                    bar.copy(
                        barFinances = bar.barFinances.copy(
                            amountPaid = newAmountPaid,
                            amountPending = newAmountPending,
                            paymentStatus = if (newAmountPending <= 0.0) "PAID" else "PENDING",
                            quota = newQuota
                        )
                    )
                } else {
                    val updatedTeams = bar.teams.map { team ->
                        if (team.teamId == payeeId) {
                            updated = true
                            val newAmountPaid = team.teamFinances.amountPaid + amountPaid
                            val newAmountPending = (team.teamFinances.totalAmountToPay - newAmountPaid).coerceAtLeast(0.0)

                            team.copy(
                                teamFinances = team.teamFinances.copy(
                                    amountPaid = newAmountPaid,
                                    amountPending = newAmountPending,
                                    paymentStatus = if (newAmountPending <= 0.0) "PAID" else "PENDING"
                                )
                            )
                        } else team
                    }
                    bar.copy(teams = updatedTeams)
                }
            }

            if (updated) {
                firestore.updateDocumentFields("leagues", leagueId, mapOf("bars" to updatedBars))
            }
        } catch (e: Exception) {
            println("Error updating league finances: $e")
        }
    }
}
