package pl.edu.przedmioty.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import pl.edu.przedmioty.model.CatalogItem

class ItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun itemsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("items")

    fun listen(
        userId: String,
        onItemsChanged: (List<CatalogItem>) -> Unit,
        onError: (Throwable) -> Unit,
    ): ListenerRegistration = itemsCollection(userId)
        .orderBy("updatedAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents.orEmpty().mapNotNull { document ->
                document.toObject(CatalogItem::class.java)?.copy(id = document.id)
            }
            onItemsChanged(items)
        }

    fun create(
        userId: String,
        item: CatalogItem,
        onResult: (Result<Unit>) -> Unit,
    ) {
        itemsCollection(userId).document(item.id).set(item)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun update(
        userId: String,
        item: CatalogItem,
        onResult: (Result<Unit>) -> Unit,
    ) {
        itemsCollection(userId).document(item.id).set(item)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun delete(
        userId: String,
        itemId: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        itemsCollection(userId).document(itemId).delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun newDocumentId(userId: String): String = itemsCollection(userId).document().id
}