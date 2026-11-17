package com.castro.solopan.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.FragmentProfileBinding
import com.castro.solopan.ui.auth.LoginActivity
import com.castro.solopan.ui.auth.RoleRouter
import kotlinx.coroutines.launch

/**
 * Perfil del usuario con sesión abierta. Lo usan los tres roles: carga los datos desde
 * Firestore, permite editar nombre y teléfono, cerrar sesión y desactivar la cuenta.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etEmail.isEnabled = false   // El correo no se edita desde aquí (RF-17 exige reautenticación)
        loadProfile()

        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnLogout.setOnClickListener {
            UserRepository.logout()
            goToLogin()
        }
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_confirm) { _, _ -> deactivateAccount() }
                .setNegativeButton(R.string.dialog_cancel, null)
                .show()
        }
    }

    /** Lee el perfil desde Firestore y llena la pantalla. */
    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = UserRepository.fetchCurrent()
                binding.tvProfileName.text = profile.name
                binding.tvRole.text = roleLabel(profile.role)
                binding.etName.setText(profile.name)
                binding.etEmail.setText(profile.email)
                binding.etPhone.setText(profile.phone)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), RoleRouter.messageFor(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Guarda nombre y teléfono en Firestore. */
    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        if (name.isBlank()) {
            binding.etName.error = getString(R.string.error_required)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                UserRepository.updateContact(name, binding.etPhone.text.toString().trim())
                binding.tvProfileName.text = name
                Toast.makeText(requireContext(), R.string.msg_saved, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), RoleRouter.messageFor(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Eliminación lógica de la cuenta y regreso al login. */
    private fun deactivateAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                UserRepository.deactivateCurrent()
                goToLogin()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), RoleRouter.messageFor(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun roleLabel(role: String): String = getString(
        when (role) {
            UserProfile.ROLE_ADMIN -> R.string.role_admin
            UserProfile.ROLE_SELLER -> R.string.role_seller
            else -> R.string.role_buyer
        }
    )

    private fun goToLogin() {
        startActivity(
            Intent(requireContext(), LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}