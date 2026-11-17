package com.castro.solopan.ui.auth

import android.content.Context
import android.content.Intent
import com.castro.solopan.R
import com.castro.solopan.data.NoProfileException
import com.castro.solopan.data.UserProfile
import com.castro.solopan.ui.admin.AdminActivity
import com.castro.solopan.ui.buyer.BuyerActivity
import com.castro.solopan.ui.seller.SellerActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

/** Decide a qué módulo entra cada usuario según el rol guardado en Firestore. */
object RoleRouter {

    /** Intent hacia el módulo del rol; limpia la pila para que "atrás" no regrese al login. */
    fun homeIntent(context: Context, role: String): Intent {
        val destination = when (role) {
            UserProfile.ROLE_ADMIN -> AdminActivity::class.java
            UserProfile.ROLE_SELLER -> SellerActivity::class.java
            else -> BuyerActivity::class.java
        }
        return Intent(context, destination)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    /** Traduce una excepción de Firebase a un mensaje entendible para el usuario. */
    fun messageFor(error: Exception): Int = when (error) {
        is FirebaseAuthUserCollisionException -> R.string.msg_email_in_use
        is FirebaseNetworkException -> R.string.msg_network
        is NoProfileException -> R.string.msg_no_profile
        is com.google.firebase.auth.FirebaseAuthException -> R.string.msg_login_error
        else -> R.string.msg_generic_error
    }
}