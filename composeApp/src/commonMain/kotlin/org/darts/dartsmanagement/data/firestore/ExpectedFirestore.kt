package org.darts.dartsmanagement.data.firestore

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp

expect class ExpectedFirestore {
    suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String
    suspend fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>): String
    suspend fun updateDocumentFields(collectionPath: String, documentId: String, data: Map<String, Any>) // New function for partial updates
    suspend fun getDocument(collectionPath: String, documentId: String): DocumentSnapshot?

    suspend fun getDocuments(
        collectionPath: String,
        whereField: String,
        whereValue: Any
    ): List<DocumentSnapshot>

    suspend fun getDocuments(collectionPath: String): List<DocumentSnapshot>

    // New function for date range queries
    suspend fun getDocumentsInDateRange(
        collectionPath: String,
        dateField: String,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp
    ): List<DocumentSnapshot>

    fun getCurrentUserUID(): String?
    // Add other Firestore operations as needed (get, update, delete, etc.)
}
