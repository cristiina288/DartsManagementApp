package org.darts.dartsmanagement.ui.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

enum class CollectionInputMode {
    COLLECTION,
    COUNTER
}

data class MachineCollectionEntry(
    val machineId: Int? = null,
    val counter: Int? = null, // Old counter
    val collectionAmounts: CollectionAmountsModel? = null,
    val inputMode: CollectionInputMode = CollectionInputMode.COLLECTION,
    val recaudacionInput: String = "",
    val contadorInput: String = ""
)

data class LeaguePaymentEntry(
    val leagueId: String = "",
    val leagueName: String = "",
    val amount: String = "",
    val isPending: Boolean = true
)

data class CollectionsState (
    val machineEntries: List<MachineCollectionEntry> = emptyList(),
    val barId: String? = null,
    val comments: String? = null,
    val snackbarMessage: String? = null,
    val globalExtraPayment: Double = 0.0,
    val leaguePayments: List<LeaguePaymentEntry> = emptyList(),
    val availableLeagues: List<org.darts.dartsmanagement.domain.leagues.models.LeagueModel> = emptyList(),
    val showLeagueValidationDialog: Boolean = false,
    val pendingLeagueIndex: Int? = null,
    // Keep these for backward compatibility during transition if needed, 
    // but better to move everything to machineEntries
    val collectionAmounts: CollectionAmountsModel? = null,
    val counter: Int? = null,
    val machineId: Int? = null
)
