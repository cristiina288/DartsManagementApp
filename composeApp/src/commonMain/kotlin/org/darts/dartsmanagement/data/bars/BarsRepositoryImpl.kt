package org.darts.dartsmanagement.data.bars

import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.domain.bars.BarsRepository
import org.darts.dartsmanagement.domain.bars.models.BarModel

import org.darts.dartsmanagement.domain.machines.MachinesRepository

class BarsRepositoryImpl(
    private val api: BarsApiService,
    private val machinesRepository: MachinesRepository
): BarsRepository {

    override suspend fun getBars(): List<BarModel> {
        return api.getBars().map { m -> m.toDomain() }
    }

    override suspend fun getBar(barId: String): Result<BarModel> {
        return runCatching {
            api.getBar(barId).toDomain()
        }
    }

    override suspend fun saveBar(saveBarRequest: SaveBarRequest) {
        return api.saveBar(saveBarRequest)
    }

    override suspend fun updateBar(barId: String, saveBarRequest: SaveBarRequest): Result<Unit> {
        return runCatching {
            api.updateBar(barId, saveBarRequest)
        }
    }

    override suspend fun updateBarMachines(barId: String, machineIds: List<Int>): Result<Unit> {
        return runCatching {
            api.updateBarMachines(barId, machineIds)
        }
    }

    override suspend fun deleteBar(barId: String): Result<Unit> {
        return runCatching {
            // 1. Get bar details to check for assigned machines
            val bar = api.getBar(barId).toDomain()
            val machineIds = bar.machines.mapNotNull { it.id }

            // 2. Delete the bar
            api.deleteBar(barId)

            // 3. If it had machines, disassociate them
            machineIds.forEach { machineId ->
                machinesRepository.getMachine(machineId).onSuccess { machine ->
                    val updatedMachine = machine.copy(barId = "")
                    machinesRepository.updateMachine(updatedMachine)
                }
            }
        }
    }
}