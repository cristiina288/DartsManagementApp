package org.darts.dartsmanagement.di

import org.darts.dartsmanagement.ui.home.HomeViewModel
import org.darts.dartsmanagement.ui.home.bars.detail.BarViewModel
import org.darts.dartsmanagement.ui.home.bars.newBar.NewBarViewModel
import org.darts.dartsmanagement.ui.home.bars.listing.BarsListingViewModel
import org.darts.dartsmanagement.ui.home.collections.CollectionsViewModel
import org.darts.dartsmanagement.ui.home.locations.listing.LocationsListingViewModel
import org.darts.dartsmanagement.ui.home.machines.detail.MachineViewModel
import org.darts.dartsmanagement.ui.home.machines.listing.MachinesListingViewModel
import org.darts.dartsmanagement.ui.home.machines.newMachine.NewMachineViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module


val uiModule = module {
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::BarsListingViewModel)
    viewModelOf(::LocationsListingViewModel)
    viewModelOf(::MachinesListingViewModel)
    viewModelOf(::BarViewModel)
    viewModelOf(::NewBarViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MachineViewModel)
    viewModelOf(::NewMachineViewModel)
    //    viewModelOf(::CollectionsHistoryListingViewModel)

}