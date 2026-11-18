package com.castro.solopan.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.R
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.FragmentUsersBinding
import com.castro.solopan.databinding.ItemUserBinding
import kotlinx.coroutines.launch

/**
 * Lista de usuarios para el Administrador: lee la colección "users" de Firestore,
 * permite buscar, filtrar por rol, crear, editar y eliminar.
 */
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    /** Copia local de los usuarios para filtrar y buscar sin volver a consultar. */
    private var allUsers: List<UserProfile> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.btnNewUser.setOnClickListener { openForm(null) }
        binding.etSearch.doAfterTextChanged { render() }
        binding.toggleFilter.addOnButtonCheckedListener { _, _, _ -> render() }
    }

    /** Recarga desde Firestore cada vez que se vuelve a la pantalla (tras crear o editar). */
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allUsers = UserRepository.listAll()
                render()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.msg_users_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Aplica búsqueda y filtro de rol sobre la lista cargada. */
    private fun render() {
        if (_binding == null) return
        val query = binding.etSearch.text.toString().trim().lowercase()
        val role = when {
            binding.btnBuyers.isChecked -> UserProfile.ROLE_BUYER
            binding.btnBakeries.isChecked -> UserProfile.ROLE_SELLER
            else -> null
        }
        val list = allUsers.filter { user ->
            (role == null || user.role == role) &&
                    (user.name.lowercase().contains(query) || user.email.lowercase().contains(query))
        }
        binding.rvUsers.adapter = UserAdapter(
            list,
            onEdit = { openForm(it.uid) },
            onDelete = { confirmDelete(it) }
        )
    }

    private fun openForm(uid: String?) {
        val intent = Intent(requireContext(), UserFormActivity::class.java)
        uid?.let { intent.putExtra(UserFormActivity.EXTRA_UID, it) }
        startActivity(intent)
    }

    /** Pide confirmación y elimina el perfil en Firestore. */
    private fun confirmDelete(user: UserProfile) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_user_title)
            .setMessage(R.string.dialog_delete_user_message)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        UserRepository.deleteProfile(user.uid)
                        Toast.makeText(requireContext(), R.string.msg_user_deleted, Toast.LENGTH_SHORT).show()
                        load()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), R.string.msg_generic_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Adaptador de la lista de usuarios: una fila por perfil con acciones de editar y eliminar. */
class UserAdapter(
    private val users: List<UserProfile>,
    private val onEdit: (UserProfile) -> Unit,
    private val onDelete: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        val roleLabel = context.getString(
            when (user.role) {
                UserProfile.ROLE_ADMIN -> R.string.role_admin
                UserProfile.ROLE_SELLER -> R.string.role_seller
                else -> R.string.role_buyer
            }
        )
        holder.binding.tvUserName.text = user.name
        holder.binding.tvUserInfo.text = "$roleLabel · ${user.email}"
        holder.binding.tvUserStatus.text =
            context.getString(if (user.active) R.string.status_active else R.string.status_suspended)
        holder.binding.tvUserStatus.setTextColor(
            context.getColor(if (user.active) R.color.success_green else R.color.error_red)
        )
        holder.binding.btnEdit.setOnClickListener { onEdit(user) }
        holder.binding.btnDelete.setOnClickListener { onDelete(user) }
    }
}