package org.darts.dartsmanagement.data.firestore

expect class ExpectedFirestore {
    suspend fun addDocument(collectionPath: String, data: Map<String, Any>): String
    // Add other Firestore operations as needed (get, update, delete, etc.)
}
