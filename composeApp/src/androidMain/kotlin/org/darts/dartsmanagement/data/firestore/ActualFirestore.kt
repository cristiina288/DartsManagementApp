package org.darts.dartsmanagement.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.DocumentSnapshot
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

    actual fun getCurrentUserUID(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
