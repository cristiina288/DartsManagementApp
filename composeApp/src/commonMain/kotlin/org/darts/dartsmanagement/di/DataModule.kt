package org.darts.dartsmanagement.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.darts.dartsmanagement.data.auth.FirebaseAuthRepository
import org.darts.dartsmanagement.data.auth.SessionManager
import org.darts.dartsmanagement.data.bars.BarsApiService
import org.darts.dartsmanagement.data.bars.BarsRepositoryImpl
import org.darts.dartsmanagement.data.characters.ApiService
import org.darts.dartsmanagement.data.characters.RepositoryImpl
import org.darts.dartsmanagement.data.collections.CollectionsApiService
import org.darts.dartsmanagement.data.collections.CollectionsRepositoryImpl
import org.darts.dartsmanagement.data.locations.LocationsApiService
import org.darts.dartsmanagement.data.locations.LocationsRepositoryImpl
import org.darts.dartsmanagement.data.machines.MachinesApiService
import org.darts.dartsmanagement.data.machines.MachinesRepositoryImpl
import org.darts.dartsmanagement.domain.auth.AuthRepository
import org.darts.dartsmanagement.domain.bars.BarsRepository
import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.UpdateBarMachinesUseCase
import org.darts.dartsmanagement.domain.characters.Repository
import org.darts.dartsmanagement.domain.collections.CollectionsRepository
import org.darts.dartsmanagement.domain.collections.DeleteCollectionUseCase
import org.darts.dartsmanagement.domain.collections.GetCollectionsByMachineId
import org.darts.dartsmanagement.domain.collections.GetCollectionsForMonth
import org.darts.dartsmanagement.domain.locations.GetLocation
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.locations.LocationsRepository
import org.darts.dartsmanagement.domain.locations.UpdateLocation
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.darts.dartsmanagement.domain.machines.UpdateMachineStatusUseCase
import org.darts.dartsmanagement.domain.machines.usecases.UpdateMachineUseCase
import org.darts.dartsmanagement.ui.bars.edit.EditBarViewModel
import org.darts.dartsmanagement.ui.collections.CollectionsViewModel
import org.darts.dartsmanagement.ui.locations.detail.LocationViewModel
import org.darts.dartsmanagement.ui.locations.edit.EditLocationViewModel
import org.darts.dartsmanagement.ui.machines.detail.MachineViewModel
import org.darts.dartsmanagement.ui.machines.edit.EditMachineViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { SessionManager() }
    factory<AuthRepository> { FirebaseAuthRepository(get(), get()) }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true}, contentType = ContentType.Any)
            }

            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "dartsmanagement-development.onrender.com"
                    //por si hay que pasar parametros: parameters.append("apiKey", "")
                }
            }
        }
    }

    factoryOf(::ApiService)
    factory<Repository> { RepositoryImpl(get()) }




    factory { BarsApiService(get(), get()) }
    factory<BarsRepository> { BarsRepositoryImpl(get()) }
    factoryOf(::GetBars) // Add the GetBars use case here


    factory { CollectionsApiService(get(), get()) }
    factory<CollectionsRepository> { CollectionsRepositoryImpl(get()) }
    factoryOf(::GetCollectionsForMonth) // Add the new use case here


    factory { LocationsApiService(get(), get()) }
    factory<LocationsRepository> { LocationsRepositoryImpl(get()) }
    factoryOf(::GetLocations)
    factoryOf(::GetLocation)
    factoryOf(::UpdateLocation)

    factory { MachinesApiService(get(), get()) }
    factory<MachinesRepository> { MachinesRepositoryImpl(get()) }
    factoryOf(::GetMachines) // Add the GetMachines use case here
    factoryOf(::UpdateMachineUseCase)
    factoryOf(::UpdateMachineStatusUseCase)
    factoryOf(::GetCollectionsByMachineId)
    factoryOf(::DeleteCollectionUseCase)
    factoryOf(::UpdateBarMachinesUseCase)

    viewModel { parameters -> CollectionsViewModel(parameters.getOrNull(), get(), get(), get()) }
    viewModel { parameters -> MachineViewModel(parameters.get(), get(), get(), get(), get()) }
    viewModel { parameters -> EditMachineViewModel(parameters.get(), get(), get()) }
    viewModel { parameters -> EditBarViewModel(parameters.get(), get(), get(), get()) }
    viewModel { parameters -> LocationViewModel(parameters.get(), get(), get()) }
    viewModel { parameters -> EditLocationViewModel(parameters.get(), get()) }
}