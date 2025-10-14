package org.darts.dartsmanagement.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

actual class ExpectedFirestore(private val firestore: FirebaseFirestore) {
    actual suspend fun addDocument(collectionPath: String, data: Map<String, Any>): String {
        val documentRef = firestore.collection(collectionPath).add(data).await()
        return documentRef.id
    }
}
