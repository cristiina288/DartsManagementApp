package org.darts.dartsmanagement.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot // Import QuerySnapshot
import dev.gitlive.firebase.firestore.Timestamp

actual class ExpectedFirestore {
    private val firestore: FirebaseFirestore = Firebase.firestore

    actual suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String {
        val documentRef = firestore.collection(collectionPath).add(data)
        return documentRef.id
    }

    actual suspend fun setDocument(collectionPath: String, documentId: String, data: Map<String, Any?>) {
        firestore.collection(collectionPath).document(documentId).set(data)
    }

    actual suspend fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>): String {
        firestore.collection(collectionPath).document(documentId).set(data)
        return documentId
    }

    actual suspend fun updateDocumentFields(collectionPath: String, documentId: String, data: Map<String, Any>) {
        firestore.collection(collectionPath).document(documentId).update(data)
    }

    actual suspend fun getDocument(collectionPath: String, documentId: String): DocumentSnapshot? {
        return firestore.collection(collectionPath).document(documentId).get()
    }

    actual suspend fun getDocuments(
        collectionPath: String,
        whereField: String,
        whereValue: Any
    ): List<DocumentSnapshot> {
        return firestore.collection(collectionPath).where(whereField, "==", whereValue).get().documents
    }

    actual suspend fun getDocuments(collectionPath: String): List<DocumentSnapshot> {
        return firestore.collection(collectionPath).get().documents
    }

    actual suspend fun getDocumentsInDateRange(
        collectionPath: String,
        dateField: String,
        startTimestamp: Timestamp,
        endTimestamp: Timestamp
    ): List<DocumentSnapshot> {
        return firestore.collection(collectionPath)
            .orderBy(dateField)
            .startAt(startTimestamp)
            .endAt(endTimestamp)
            .get()
            .documents
    }

    actual fun getCurrentUserUID(): String? {
        return Firebase.auth.currentUser?.uid
    }

    // New query builder methods (iOS implementation - placeholder for now)
    actual fun getDocumentsQuery(collectionPath: String): FirestoreQuery {
        // Placeholder, needs proper iOS implementation if query builder is fully used
        return IosFirestoreQuery(firestore.collection(collectionPath))
    }

    // New method for batch fetching by IDs (iOS implementation - placeholder for now)
    actual suspend fun getDocumentsByIds(collectionPath: String, ids: List<String>): List<DocumentSnapshot> {
        if (ids.isEmpty()) return emptyList()
        // Firebase iOS SDK might have limitations on `whereIn` for document IDs or require specific handling
        // This is a simplified placeholder. A full implementation would handle chunking for large 'ids' list and
        // potentially query by a field that stores the 'id' if direct document ID 'whereIn' is not supported
        return firestore.collection(collectionPath)
            .where("id", "in", ids) // Assuming 'id' field stores the document ID
            .get()
            .documents
    }

    actual enum class Direction {
        ASCENDING, DESCENDING
    }
}

// iOS-specific implementation of FirestoreQuery (placeholder)
class IosFirestoreQuery(private var query: dev.gitlive.firebase.firestore.Query) : FirestoreQuery {
    override fun orderBy(field: String, direction: ExpectedFirestore.Direction): FirestoreQuery {
        query = when (direction) {
            ExpectedFirestore.Direction.ASCENDING -> query.orderBy(field)
            ExpectedFirestore.Direction.DESCENDING -> query.orderBy(field, descending = true)
        }
        return this
    }

    override fun limit(limit: Int): FirestoreQuery {
        query = query.limit(limit)
        return this
    }

    override fun startAfter(value: Timestamp): FirestoreQuery {
        query = query.startAfter(value)
        return this
    }

    override fun startAfter(vararg values: Any): FirestoreQuery {
        query = query.startAfter(*values)
        return this
    }

    override suspend fun get(): QuerySnapshot {
        return query.get()
    }
}