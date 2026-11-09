package com.castro.solopan.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.castro.solopan.R
import com.castro.solopan.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnCreate.setOnClickListener { validateAndRegister() }

        binding.toggleRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                binding.layoutSeller.visibility =
                    if (checkedId == R.id.btnRoleBakery) View.VISIBLE else View.GONE
            }
        }
    }

    private fun validateAndRegister() {
        val isBakery = binding.layoutSeller.visibility == View.VISIBLE
        val password = binding.etPassword.text.toString()
        val required = mutableListOf(binding.etName)
        if (isBakery) required += listOf(binding.etBakeryName, binding.etNit, binding.etSchedule)

        for (field in required) {
            if (field.text.toString().isBlank()) {
                field.error = getString(R.string.error_required)
                return
            }
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()) {
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
        Toast.makeText(this, R.string.msg_account_created, Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        private const val PHONE_LENGTH = 10
        private const val MIN_PASSWORD_LENGTH = 8
    }
}