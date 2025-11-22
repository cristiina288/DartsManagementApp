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
import org.darts.dartsmanagement.domain.characters.Repository
import org.darts.dartsmanagement.domain.collections.CollectionsRepository
import org.darts.dartsmanagement.domain.collections.GetCollectionsForMonth
import org.darts.dartsmanagement.domain.locations.LocationsRepository
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    factory<AuthRepository> { FirebaseAuthRepository(get()) }

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




    factory { BarsApiService(get()) }
    factory<BarsRepository> { BarsRepositoryImpl(get()) }
    factoryOf(::GetBars) // Add the GetBars use case here


    factoryOf(::CollectionsApiService)
    factory<CollectionsRepository> { CollectionsRepositoryImpl(get()) }
    factoryOf(::GetCollectionsForMonth) // Add the new use case here


    factory { LocationsApiService(get()) }
    factory<LocationsRepository> { LocationsRepositoryImpl(get()) }

    factory { MachinesApiService(get()) }
    factory<MachinesRepository> { MachinesRepositoryImpl(get()) }
    factoryOf(::GetMachines) // Add the GetMachines use case here
}