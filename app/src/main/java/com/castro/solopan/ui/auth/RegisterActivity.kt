package com.castro.solopan.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

/**
 * Registro de Comprador o Vendedor (panadería). Valida los datos, crea la cuenta en
 * Firebase Authentication y guarda el perfil con su rol en Firestore.
 * El rol Administrador no se puede elegir aquí: lo asigna un administrador existente.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnCreate.setOnClickListener { validateAndRegister() }

        // Muestra los campos extra de la panadería solo cuando se elige ese rol
        binding.toggleRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                binding.layoutSeller.visibility =
                    if (checkedId == R.id.btnRoleBakery) View.VISIBLE else View.GONE
            }
        }
    }

    /** Valida el formulario campo por campo y, si todo es correcto, crea la cuenta. */
    private fun validateAndRegister() {
        val isBakery = binding.layoutSeller.visibility == View.VISIBLE
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        val required = mutableListOf(binding.etName)
        if (isBakery) required += listOf(binding.etBakeryName, binding.etNit, binding.etSchedule)
        for (field in required) {
            if (field.text.toString().isBlank()) {
                field.error = getString(R.string.error_required)
                return
            }
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            return
        }
        if (binding.etPhone.text.toString().filter { it.isDigit() }.length != PHONE_LENGTH) {
            binding.etPhone.error = getString(R.string.error_phone_invalid)
            return
        }
        val validPassword = password.length >= MIN_PASSWORD_LENGTH &&
                password.any { it.isLetter() } && password.any { it.isDigit() }
        if (!validPassword) {
            binding.etPassword.error = getString(R.string.error_password_format)
            return
        }
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, R.string.error_terms, Toast.LENGTH_SHORT).show()
            return
        }

        val profile = UserProfile(
            name = binding.etName.text.toString().trim(),
            email = email,
            phone = binding.etPhone.text.toString().filter { it.isDigit() },
            role = if (isBakery) UserProfile.ROLE_SELLER else UserProfile.ROLE_BUYER,
            bakeryName = binding.etBakeryName.text.toString().trim(),
            nit = binding.etNit.text.toString().trim(),
            schedule = binding.etSchedule.text.toString().trim()
        )
        createAccount(email, password, profile)
    }

    /** Crea la cuenta y entra directamente al módulo del rol elegido. */
    private fun createAccount(email: String, password: String, profile: UserProfile) {
        binding.btnCreate.isEnabled = false
        lifecycleScope.launch {
            try {
                val created = UserRepository.register(email, password, profile)
                Toast.makeText(this@RegisterActivity, R.string.msg_account_created, Toast.LENGTH_SHORT).show()
                startActivity(RoleRouter.homeIntent(this@RegisterActivity, created.role))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, RoleRouter.messageFor(e), Toast.LENGTH_LONG).show()
            } finally {
                binding.btnCreate.isEnabled = true
            }
        }
    }

    companion object {
        private const val PHONE_LENGTH = 10
        private const val MIN_PASSWORD_LENGTH = 8
    }
}