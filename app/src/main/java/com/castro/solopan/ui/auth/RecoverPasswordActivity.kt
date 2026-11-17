package com.castro.solopan.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.ActivityRecoverPasswordBinding
import kotlinx.coroutines.launch

/** Envía por correo un enlace de restablecimiento de contraseña (Firebase Authentication). */
class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendLink() }
    }

    private fun sendLink() {
        val email = binding.etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            return
        }
        binding.btnSend.isEnabled = false
        lifecycleScope.launch {
            try {
                UserRepository.sendPasswordReset(email)
            } catch (e: Exception) {
                // Se ignora el error a propósito: el mensaje no revela si el correo existe (RF-5)
            }
            Toast.makeText(this@RecoverPasswordActivity, R.string.msg_recover_sent, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}