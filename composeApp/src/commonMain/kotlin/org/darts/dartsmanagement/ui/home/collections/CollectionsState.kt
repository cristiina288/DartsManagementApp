package org.darts.dartsmanagement.ui.home.collections

import org.darts.dartsmanagement.domain.collections.models.CollectionAmountsModel

data class CollectionsState (
    //val characterOfTheDay: CharacterModel? = null
    val collectionAmounts: CollectionAmountsModel? = null,
    val counter: Int? = null,
    val machineId: Int? = null
)