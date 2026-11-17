package com.castro.solopan.data

/**
 * Perfil de un usuario guardado en Firestore (colección "users").
 * El id del documento es el uid que asigna Firebase Authentication.
 */
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ROLE_BUYER,
    val active: Boolean = true,
    val bakeryName: String = "",
    val nit: String = "",
    val schedule: String = ""
) {
    /** Convierte el perfil a un mapa para escribirlo en Firestore. */
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "email" to email,
        "phone" to phone,
        "role" to role,
        "active" to active,
        "bakeryName" to bakeryName,
        "nit" to nit,
        "schedule" to schedule
    )

    companion object {
        const val ROLE_BUYER = "buyer"
        const val ROLE_SELLER = "seller"
        const val ROLE_ADMIN = "admin"
    }
}