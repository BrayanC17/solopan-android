package com.castro.solopan.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Se lanza cuando existe la cuenta de Authentication pero no su documento en Firestore. */
class NoProfileException : Exception()

/**
 * Punto único de acceso a Firebase Authentication y a la colección "users".
 * Las pantallas llaman a este objeto y no conocen detalles de Firebase.
 */
object UserRepository {

    private const val COLLECTION = "users"
    private const val SECONDARY_APP = "admin-creator"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /** Devuelve true si Firebase conserva una sesión abierta en este dispositivo. */
    fun hasSession(): Boolean = auth.currentUser != null

    /**
     * Crea la cuenta en Authentication y guarda el perfil con su rol en Firestore.
     * Firebase deja la sesión abierta automáticamente al crear la cuenta.
     */
    suspend fun register(email: String, password: String, profile: UserProfile): UserProfile {
        val uid = auth.createUserWithEmailAndPassword(email, password).await().user!!.uid
        db.collection(COLLECTION).document(uid).set(profile.toMap()).await()
        return profile.copy(uid = uid)
    }

    /** Inicia sesión con correo y contraseña y devuelve el perfil (con su rol). */
    suspend fun login(email: String, password: String): UserProfile {
        auth.signInWithEmailAndPassword(email, password).await()
        return fetchCurrent()
    }

    /** Lee el perfil del usuario con sesión abierta. */
    suspend fun fetchCurrent(): UserProfile {
        val uid = auth.currentUser?.uid ?: throw NoProfileException()
        return fetchById(uid)
    }

    /** Lee el perfil de cualquier usuario por su uid. */
    suspend fun fetchById(uid: String): UserProfile {
        val doc = db.collection(COLLECTION).document(uid).get().await()
        if (!doc.exists()) throw NoProfileException()
        return doc.toObject(UserProfile::class.java)?.copy(uid = uid) ?: throw NoProfileException()
    }

    /** Lista todos los usuarios (uso exclusivo del Administrador), ordenados por nombre. */
    suspend fun listAll(): List<UserProfile> =
        db.collection(COLLECTION).get().await().documents
            .mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
            }
            .sortedBy { it.name.lowercase() }

    /**
     * Crea un usuario desde el panel del Administrador sin cerrar la sesión del administrador:
     * la cuenta se crea con una segunda instancia de Firebase y luego se escribe el perfil.
     */
    suspend fun createByAdmin(
        context: Context, email: String, password: String, profile: UserProfile
    ): UserProfile {
        val secondary = try {
            FirebaseApp.getInstance(SECONDARY_APP)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                context.applicationContext, FirebaseApp.getInstance().options, SECONDARY_APP
            )
        }
        val secondaryAuth = FirebaseAuth.getInstance(secondary)
        val uid = secondaryAuth.createUserWithEmailAndPassword(email, password).await().user!!.uid
        secondaryAuth.signOut()
        db.collection(COLLECTION).document(uid).set(profile.toMap()).await()
        return profile.copy(uid = uid)
    }

    /** Actualiza nombre, rol y estado de un usuario (edición desde el Administrador). */
    suspend fun updateByAdmin(uid: String, name: String, role: String, active: Boolean) {
        db.collection(COLLECTION).document(uid)
            .update(mapOf("name" to name, "role" to role, "active" to active)).await()
    }

    /**
     * Elimina el perfil del usuario en Firestore. Sin perfil la cuenta ya no puede entrar
     * a la app. (Borrar la cuenta de Authentication requiere un servidor con Admin SDK.)
     */
    suspend fun deleteProfile(uid: String) {
        db.collection(COLLECTION).document(uid).delete().await()
    }

    /** Actualiza nombre y teléfono del usuario actual. */
    suspend fun updateContact(name: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(COLLECTION).document(uid)
            .update(mapOf("name" to name, "phone" to phone)).await()
    }

    /** Eliminación lógica: marca la cuenta como inactiva y cierra la sesión (RF-18). */
    suspend fun deactivateCurrent() {
        val uid = auth.currentUser?.uid ?: return
        db.collection(COLLECTION).document(uid).update("active", false).await()
        auth.signOut()
    }

    /** Envía el correo de recuperación de contraseña. */
    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /** Cierra la sesión del dispositivo. */
    fun logout() = auth.signOut()
}