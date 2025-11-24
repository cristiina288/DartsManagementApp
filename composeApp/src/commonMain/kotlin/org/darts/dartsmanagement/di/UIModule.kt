package org.darts.dartsmanagement.di

import org.darts.dartsmanagement.ui.auth.AuthViewModel
import org.darts.dartsmanagement.ui.bars.detail.BarViewModel
import org.darts.dartsmanagement.ui.bars.edit.EditBarViewModel
import org.darts.dartsmanagement.ui.bars.listing.BarsListingViewModel
import org.darts.dartsmanagement.ui.bars.newBar.NewBarViewModel
import org.darts.dartsmanagement.ui.collections.CollectionsViewModel
import org.darts.dartsmanagement.ui.home.HomeViewModel
import org.darts.dartsmanagement.ui.locations.detail.LocationViewModel
import org.darts.dartsmanagement.ui.locations.listing.LocationsListingViewModel
import org.darts.dartsmanagement.ui.machines.detail.MachineViewModel
import org.darts.dartsmanagement.ui.machines.listing.MachinesListingViewModel
import org.darts.dartsmanagement.ui.machines.newMachine.NewMachineViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module


val uiModule = module {
    single { AuthViewModel() }
    viewModelOf(::HomeViewModel)
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::BarsListingViewModel)
    viewModelOf(::LocationsListingViewModel)
    viewModelOf(::LocationViewModel)
    viewModelOf(::MachinesListingViewModel)
    viewModelOf(::BarViewModel)
    viewModelOf(::NewBarViewModel)
    viewModelOf(::EditBarViewModel)
    viewModelOf(::MachineViewModel)
    viewModelOf(::NewMachineViewModel)
    //    viewModelOf(::CollectionsHistoryListingViewModel)

}