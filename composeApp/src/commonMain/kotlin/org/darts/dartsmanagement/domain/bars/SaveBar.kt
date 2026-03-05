package org.darts.dartsmanagement.domain.bars

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.darts.dartsmanagement.data.bars.requests.SaveBarRequest
import org.darts.dartsmanagement.data.firestore.ExpectedFirestore

class SaveBar(
    private val barRepository: BarsRepository,
    private val firestore: ExpectedFirestore
) {

    suspend operator fun invoke(saveBarRequest: SaveBarRequest) {
        val barId = barRepository.saveBar(saveBarRequest)

        if (barId.isNotEmpty() && saveBarRequest.machineIds.isNotEmpty()) {
            runCatching {
                coroutineScope {
                    saveBarRequest.machineIds.forEach { machineId ->
                        launch {
                            val data = mapOf("barId" to barId, "status" to "ACTIVE")
                            firestore.updateDocumentFields("machines", machineId.toString(), data)
                        }
                    }
                }
            }.onFailure {
                println("Secondary operation to update machine barIds after bar creation failed: $it")
            }
        }
    }
}
