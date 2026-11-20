package com.castro.solopan.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Acceso a la colección "products" de Firestore y a la lista de panaderías
 * (usuarios con rol vendedor). Las pantallas no conocen detalles de Firestore.
 */
object ProductRepository {

    private const val PRODUCTS = "products"
    private const val USERS = "users"
    private val db = FirebaseFirestore.getInstance()

    /** Productos de una panadería, ordenados por nombre. Con onlyActive se omiten los ocultos. */
    suspend fun listByOwner(ownerUid: String, onlyActive: Boolean = false): List<Product> =
        db.collection(PRODUCTS).whereEqualTo("ownerUid", ownerUid).get().await().documents
            .mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) }
            .filter { !onlyActive || it.active }
            .sortedBy { it.name.lowercase() }

    /** Lee un producto por id (null si no existe). */
    suspend fun get(id: String): Product? {
        val doc = db.collection(PRODUCTS).document(id).get().await()
        return if (doc.exists()) doc.toObject(Product::class.java)?.copy(id = doc.id) else null
    }

    /** Crea el producto (id vacío) o lo actualiza. Devuelve el id resultante. */
    suspend fun save(product: Product): String {
        val collection = db.collection(PRODUCTS)
        return if (product.id.isBlank()) {
            collection.add(product.toMap()).await().id
        } else {
            collection.document(product.id).set(product.toMap()).await()
            product.id
        }
    }

    /** Elimina un producto. */
    suspend fun delete(id: String) {
        db.collection(PRODUCTS).document(id).delete().await()
    }

    /** Panaderías activas: usuarios con rol vendedor, ordenadas por nombre. */
    suspend fun listBakeries(): List<Bakery> =
        db.collection(USERS).whereEqualTo("role", UserProfile.ROLE_SELLER).get().await().documents
            .mapNotNull { doc ->
                val user = doc.toObject(UserProfile::class.java)
                if (user == null || !user.active) null
                else Bakery(doc.id, user.bakeryName.ifBlank { user.name }, user.name, user.schedule)
            }
            .sortedBy { it.name.lowercase() }
}