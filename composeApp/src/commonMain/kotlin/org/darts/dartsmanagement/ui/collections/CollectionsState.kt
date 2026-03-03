package org.darts.dartsmanagement.ui.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

data class MachineCollectionEntry(
    val machineId: Int? = null,
    val counter: Int? = null,
    val collectionAmounts: CollectionAmountsModel? = null
)

data class CollectionsState (
    val machineEntries: List<MachineCollectionEntry> = listOf(MachineCollectionEntry()),
    val barId: String? = null,
    val comments: String? = null,
    val snackbarMessage: String? = null,
    val globalExtraPayment: Double = 0.0,
    val selectedLeagueId: String? = null,
    val leaguePayment: Double = 0.0,
    // Keep these for backward compatibility during transition if needed,
    // but better to move everything to machineEntries
    val collectionAmounts: CollectionAmountsModel? = null,
    val counter: Int? = null,
    val machineId: Int? = null
)
