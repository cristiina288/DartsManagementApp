package org.darts.dartsmanagement.data.firestore

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.QuerySnapshot // Import QuerySnapshot

expect class ExpectedFirestore {
    suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String
    suspend fun setDocument(collectionPath: String, documentId: String, data: Map<String, Any?>)
    suspend fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>): String
    suspend fun updateDocumentFields(collectionPath: String, documentId: String, data: Map<String, Any>)
    suspend fun getDocument(collectionPath: String, documentId: String): DocumentSnapshot?

    suspend fun getDocuments(
        collectionPath: String,
        whereField: String,
        whereValue: Any
    ): List<DocumentSnapshot>

    suspend fun getDocuments(collectionPath: String): List<DocumentSnapshot>

    suspend fun getDocumentsInDateRange(
        collectionPath: String,
        dateField: String,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp
    ): List<DocumentSnapshot>

    fun getCurrentUserUID(): String?

    // New query builder methods
    fun getDocumentsQuery(collectionPath: String): FirestoreQuery

    // New method for batch fetching by IDs
    suspend fun getDocumentsByIds(collectionPath: String, ids: List<String>): List<DocumentSnapshot>

    enum class Direction {
        ASCENDING, DESCENDING
    }
}

// Interface for the query builder
interface FirestoreQuery {
    fun orderBy(field: String, direction: ExpectedFirestore.Direction): FirestoreQuery
    fun limit(limit: Int): FirestoreQuery
    fun startAfter(value: Timestamp): FirestoreQuery
    fun startAfter(vararg values: Any): FirestoreQuery // New overload
    suspend fun get(): QuerySnapshot
}