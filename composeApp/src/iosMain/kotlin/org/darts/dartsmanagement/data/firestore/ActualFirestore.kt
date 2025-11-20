package org.darts.dartsmanagement.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

actual class ExpectedFirestore {
    private val firestore: FirebaseFirestore = Firebase.firestore

    actual suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String {
        val documentRef = firestore.collection(collectionPath).add(data)
        return documentRef.id
    }

    actual suspend fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>): String {
        firestore.collection(collectionPath).document(documentId).set(data)
        return documentId
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

    actual fun getCurrentUserUID(): String? {
        return Firebase.auth.currentUser?.uid
    }
}