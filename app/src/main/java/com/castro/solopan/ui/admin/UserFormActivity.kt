package com.castro.solopan.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.ActivityUserFormBinding
import com.castro.solopan.ui.auth.RoleRouter
import kotlinx.coroutines.launch

/**
 * Formulario para crear o editar un usuario desde el Administrador.
 * Al crear se pide correo y contraseña inicial; al editar el correo no se modifica.
 */
class UserFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserFormBinding

    /** uid del usuario que se edita; null cuando se está creando uno nuevo. */
    private var editingUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingUid = intent.getStringExtra(EXTRA_UID)
        binding.tvFormTitle.setText(
            if (editingUid == null) R.string.user_form_create else R.string.user_form_edit
        )
        if (editingUid != null) {
            // En edición no se cambia el correo ni la contraseña
            binding.etPassword.visibility = View.GONE
            binding.etEmail.isEnabled = false
            loadUser(editingUid!!)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
    }

    /** Carga los datos actuales del usuario en el formulario. */
    private fun loadUser(uid: String) {
        lifecycleScope.launch {
            try {
                val user = UserRepository.fetchById(uid)
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.swActive.isChecked = user.active
                binding.toggleRole.check(
                    when (user.role) {
                        UserProfile.ROLE_ADMIN -> R.id.btnRoleAdmin
                        UserProfile.ROLE_SELLER -> R.id.btnRoleBakery
                        else -> R.id.btnRoleBuyer
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@UserFormActivity, RoleRouter.messageFor(e), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Valida y guarda: crea la cuenta o actualiza el perfil existente. */
    private fun save() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val role = when {
            binding.btnRoleAdmin.isChecked -> UserProfile.ROLE_ADMIN
            binding.btnRoleBakery.isChecked -> UserProfile.ROLE_SELLER
            else -> UserProfile.ROLE_BUYER
        }

        if (name.isBlank()) {
            binding.etName.error = getString(R.string.error_required)
            return
        }
        if (editingUid == null) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = getString(R.string.error_email_invalid)
                return
            }
            val valid = password.length >= MIN_PASSWORD_LENGTH &&
                    password.any { it.isLetter() } && password.any { it.isDigit() }
            if (!valid) {
                binding.etPassword.error = getString(R.string.error_password_format)
                return
            }
        }

        binding.btnSave.isEnabled = false
        lifecycleScope.launch {
            try {
                val uid = editingUid
                if (uid == null) {
                    val profile = UserProfile(
                        name = name, email = email, role = role, active = binding.swActive.isChecked
                    )
                    UserRepository.createByAdmin(this@UserFormActivity, email, password, profile)
                } else {
                    UserRepository.updateByAdmin(uid, name, role, binding.swActive.isChecked)
                }
                Toast.makeText(this@UserFormActivity, R.string.msg_user_saved, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@UserFormActivity, RoleRouter.messageFor(e), Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
            }
        }
    }

    companion object {
        const val EXTRA_UID = "extra_user_uid"
        private const val MIN_PASSWORD_LENGTH = 8
    }
}