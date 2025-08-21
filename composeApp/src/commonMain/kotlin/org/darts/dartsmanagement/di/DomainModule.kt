package org.darts.dartsmanagement.di

import org.darts.dartsmanagement.domain.bars.GetBars
import org.darts.dartsmanagement.domain.bars.SaveBar
import org.darts.dartsmanagement.domain.characters.GetRandomCharacter
import org.darts.dartsmanagement.domain.collections.GetCollectionsByMachineId
import org.darts.dartsmanagement.domain.collections.SaveCollection
import org.darts.dartsmanagement.domain.locations.GetLocations
import org.darts.dartsmanagement.domain.machines.GetMachines
import org.darts.dartsmanagement.domain.machines.SaveMachine
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    //factory<MachineName> { MachineName() }
    //factory<GetBar> { GetBar(get()) }
    //single<GetBar> { GetBar(get()) } si queremos que sea singleton = unica instancia

    //factoryOf(::GetBar) //LO MISMO QUE EL OTRO FACTORY PERO NO HACE FALTA PASARLE TODOS LOS GETS

    factoryOf(::GetRandomCharacter)
    factoryOf(::GetBars)
    factoryOf(::SaveCollection)
    factoryOf(::GetLocations)
    factoryOf(::GetMachines)
    factoryOf(::SaveBar)
    factoryOf(::GetCollectionsByMachineId)
    factoryOf(::SaveMachine)
}