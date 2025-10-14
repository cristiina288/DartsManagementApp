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
import org.darts.dartsmanagement.domain.characters.Repository
import org.darts.dartsmanagement.domain.collections.CollectionsRepository
import org.darts.dartsmanagement.domain.locations.LocationsRepository
import org.darts.dartsmanagement.domain.machines.MachinesRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single { dev.gitlive.firebase.Firebase.auth }
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


    factory { BarsApiService(get(), get()) }
    factory<BarsRepository> { BarsRepositoryImpl(get()) }


    factoryOf(::CollectionsApiService)
    factory<CollectionsRepository> { CollectionsRepositoryImpl(get()) }


    factoryOf(::LocationsApiService)
    factory<LocationsRepository> { LocationsRepositoryImpl(get()) }

    factoryOf(::MachinesApiService)
    factory<MachinesRepository> { MachinesRepositoryImpl(get()) }
}