package org.darts.dartsmanagement.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp as KmpTimestamp // Alias the KMP Timestamp
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

actual class ExpectedFirestore {
    lateinit var firestore: FirebaseFirestore

    actual suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String {
        val documentRef = firestore.collection(collectionPath).add(data).await()
        return documentRef.id
    }

    actual suspend fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>): String {
        firestore.collection(collectionPath).document(documentId).set(data).await()
        return documentId
    }

    actual suspend fun updateDocumentFields(collectionPath: String, documentId: String, data: Map<String, Any>) {
        firestore.collection(collectionPath).document(documentId).update(data).await()
    }

    actual suspend fun getDocument(collectionPath: String, documentId: String): DocumentSnapshot? {
        return firestore.collection(collectionPath).document(documentId).get().await()?.let { DocumentSnapshot(it) }
    }

    actual suspend fun getDocuments(
        collectionPath: String,
        whereField: String,
        whereValue: Any
    ): List<DocumentSnapshot> {
        return firestore.collection(collectionPath).whereEqualTo(whereField, whereValue).get().await().documents.map { DocumentSnapshot(it) }
    }

    actual suspend fun getDocuments(collectionPath: String): List<DocumentSnapshot> {
        return firestore.collection(collectionPath).get().await().documents.map { DocumentSnapshot(it) }
    }

    actual suspend fun getDocumentsInDateRange(
        collectionPath: String,
        dateField: String,
        startTimestamp: KmpTimestamp,
        endTimestamp: KmpTimestamp
    ): List<DocumentSnapshot> {
        return firestore.collection(collectionPath)
            .whereGreaterThanOrEqualTo(dateField, Timestamp(startTimestamp.seconds, startTimestamp.nanoseconds))
            .whereLessThanOrEqualTo(dateField, Timestamp(endTimestamp.seconds, endTimestamp.nanoseconds))
            .get()
            .await()
            .documents
            .map { DocumentSnapshot(it) }
    }

    actual fun getCurrentUserUID(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // Actual implementation for FirestoreQuery
    actual fun getDocumentsQuery(collectionPath: String): FirestoreQuery {
        return AndroidFirestoreQuery(firestore.collection(collectionPath))
    }

    // Actual implementation for getDocumentsByIds
    actual suspend fun getDocumentsByIds(collectionPath: String, ids: List<String>): List<DocumentSnapshot> {
        if (ids.isEmpty()) return emptyList()
        return firestore.collection(collectionPath)
            .whereIn("id", ids) // Assuming 'id' is the field to query by
            .get()
            .await()
            .documents
            .map { DocumentSnapshot(it) }
    }

    // Actual implementation for Direction enum
    actual enum class Direction {
        ASCENDING, DESCENDING
    }
}

// Android-specific implementation of FirestoreQuery
class AndroidFirestoreQuery(private var query: Query) : FirestoreQuery {
    override fun orderBy(field: String, direction: ExpectedFirestore.Direction): FirestoreQuery {
        query = when (direction) {
            ExpectedFirestore.Direction.ASCENDING -> query.orderBy(field, Query.Direction.ASCENDING)
            ExpectedFirestore.Direction.DESCENDING -> query.orderBy(field, Query.Direction.DESCENDING)
        }
        return this
    }

    override fun limit(limit: Int): FirestoreQuery {
        query = query.limit(limit.toLong())
        return this
    }

    override fun startAfter(value: KmpTimestamp): FirestoreQuery {
        query = query.startAfter(Timestamp(value.seconds, value.nanoseconds))
        return this
    }

    override suspend fun get(): QuerySnapshot {
        return QuerySnapshot(query.get().await())
    }
}