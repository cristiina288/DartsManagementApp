package org.darts.dartsmanagement.ui.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

data class CollectionsState (
    //val characterOfTheDay: CharacterModel? = null
    val collectionAmounts: CollectionAmountsModel? = null,
    val counter: Int? = null,
    val machineId: Int? = null,
    val barId: String? = null,
    val comments: String? = null,
    val snackbarMessage: String? = null
)

