package com.castro.solopan.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.castro.solopan.R
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.AppUser
import com.castro.solopan.databinding.ActivityUserFormBinding

class UserFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserFormBinding
    private var editingId = NEW_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getIntExtra(EXTRA_ID, NEW_USER)
        val existing = AdminStore.findUser(editingId)
        binding.tvFormTitle.setText(
            if (existing == null) R.string.user_form_create else R.string.user_form_edit
        )
        existing?.let {
            binding.etName.setText(it.name)
            binding.etEmail.setText(it.email)
            binding.swActive.isChecked = it.active
            binding.toggleRole.check(
                if (it.role == "Panadería") R.id.btnRoleBakery else R.id.btnRoleBuyer
            )
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        if (name.isBlank()) {
            binding.etName.error = getString(R.string.error_required)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_email_invalid)
            return
        }
        val role = if (binding.btnRoleBakery.isChecked) "Panadería" else "Comprador"
        val id = if (editingId == NEW_USER) AdminStore.nextUserId() else editingId
        AdminStore.upsert(AppUser(id, name, email, role, binding.swActive.isChecked))
        Toast.makeText(this, R.string.msg_user_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_ID = "extra_user_id"
        private const val NEW_USER = -1
    }
}