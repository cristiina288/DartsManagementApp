package org.darts.dartsmanagement.domain.bars

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore
import org.darts.dartsmanagement.domain.common.models.Status

class UpdateBarMachinesUseCase(
    private val barsRepository: BarsRepository,
    private val firestore: ExpectedFirestore
) {
    suspend operator fun invoke(barId: String, machineIds: List<Int>): Result<Unit> {
        // First, get the old state of the bar to find out which machines were assigned to it
        val oldMachineIdsResult = barsRepository.getBar(barId)
        if (oldMachineIdsResult.isFailure) {
            return Result.failure(oldMachineIdsResult.exceptionOrNull() ?: IllegalStateException("Could not fetch old bar state"))
        }
        val oldMachineIds = oldMachineIdsResult.getOrThrow().machines.mapNotNull { it.id?.toInt() }.toSet()

        // The main operation: update the bar's own list of machines
        val barUpdateResult = barsRepository.updateBarMachines(barId, machineIds)

        // If the main operation is successful, trigger the secondary updates on the machines themselves
        if (barUpdateResult.isSuccess) {
            val newMachineIds = machineIds.toSet()

            val machinesToRemoveBarId = oldMachineIds - newMachineIds
            val machinesToAddBarId = newMachineIds - oldMachineIds

            // Use runCatching for the secondary operations, so they don't fail the primary one
            runCatching {
                coroutineScope {
                    machinesToRemoveBarId.forEach { machineId ->
                        launch {
                            val data = mapOf("barId" to "", "status.id" to Status.INACTIVE.id)
                            firestore.updateDocumentFields("machines", machineId.toString(), data)
                        }
                    }
                    machinesToAddBarId.forEach { machineId ->
                        launch {
                            val data = mapOf("barId" to barId, "status.id" to Status.ACTIVE.id)
                            firestore.updateDocumentFields("machines", machineId.toString(), data)
                        }
                    }
                }
            }.onFailure {
                // Log the error for the secondary operation, but don't fail the whole use case
                println("Secondary operation to update machine barIds failed: $it")
            }
        }

        return barUpdateResult
    }
}