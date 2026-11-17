package com.castro.solopan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión. Soporta correo y contraseña (Firebase Authentication)
 * y huella dactilar, que reutiliza la sesión que Firebase conserva en el dispositivo.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBiometric()

        binding.btnLogin.setOnClickListener { validateAndLogin() }
        binding.btnGoogle.setOnClickListener { showMessage(R.string.msg_google_soon) }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /** Valida el formato de los campos y, si es correcto, autentica contra Firebase. */
    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.etPassword.error = getString(R.string.error_password_short)
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                openHome(UserRepository.login(email, password))
            } catch (e: Exception) {
                showMessage(RoleRouter.messageFor(e))
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Configura el botón de huella. Solo aparece si el dispositivo tiene sensor con huella
     * registrada Y ya existe una sesión guardada por un inicio de sesión previo con contraseña.
     */
    private fun setupBiometric() {
        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val available = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS

        if (!available) {
            binding.btnBiometric.visibility = View.GONE
            return
        }

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    enterWithExistingSession()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        binding.btnBiometric.setOnClickListener {
            if (UserRepository.hasSession()) {
                prompt.authenticate(promptInfo)
            } else {
                showMessage(R.string.msg_biometric_needs_session)
            }
        }
    }

    /** Tras validar la huella, entra con la sesión de Firebase y lee el rol. */
    private fun enterWithExistingSession() {
        lifecycleScope.launch {
            try {
                openHome(UserRepository.fetchCurrent())
            } catch (e: Exception) {
                showMessage(RoleRouter.messageFor(e))
            }
        }
    }

    /** Abre el módulo del rol; si la cuenta está suspendida cierra la sesión y avisa. */
    private fun openHome(profile: UserProfile) {
        if (!profile.active) {
            UserRepository.logout()
            showMessage(R.string.msg_user_inactive)
            return
        }
        startActivity(RoleRouter.homeIntent(this, profile.role))
        finish()
    }

    /** Evita pulsaciones repetidas mientras se espera la respuesta del servidor. */
    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
    }

    private fun showMessage(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }
}