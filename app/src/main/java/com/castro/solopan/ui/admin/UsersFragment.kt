package com.castro.solopan.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.R
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.AppUser
import com.castro.solopan.databinding.FragmentUsersBinding
import com.castro.solopan.databinding.ItemUserBinding

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.btnNewUser.setOnClickListener { openForm(NEW_USER) }
        binding.etSearch.doAfterTextChanged { refresh() }
        binding.toggleFilter.addOnButtonCheckedListener { _, _, _ -> refresh() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val role = when {
            binding.btnBuyers.isChecked -> "Comprador"
            binding.btnBakeries.isChecked -> "Panadería"
            else -> null
        }
        val list = AdminStore.users.filter { user ->
            (role == null || user.role == role) &&
                    (user.name.lowercase().contains(query) || user.email.lowercase().contains(query))
        }
        binding.rvUsers.adapter = UserAdapter(
            list,
            onEdit = { openForm(it.id) },
            onDelete = { confirmDelete(it) }
        )
    }

    private fun openForm(id: Int) {
        startActivity(
            Intent(requireContext(), UserFormActivity::class.java)
                .putExtra(UserFormActivity.EXTRA_ID, id)
        )
    }

    private fun confirmDelete(user: AppUser) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_user_title)
            .setMessage(R.string.dialog_delete_user_message)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                AdminStore.deleteUser(user.id)
                refresh()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val NEW_USER = -1
    }
}

class UserAdapter(
    private val users: List<AppUser>,
    private val onEdit: (AppUser) -> Unit,
    private val onDelete: (AppUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        holder.binding.tvUserName.text = user.name
        holder.binding.tvUserInfo.text = "${user.role} · ${user.email}"
        holder.binding.tvUserStatus.text =
            context.getString(if (user.active) R.string.status_active else R.string.status_suspended)
        holder.binding.tvUserStatus.setTextColor(
            context.getColor(if (user.active) R.color.success_green else R.color.error_red)
        )
        holder.binding.btnEdit.setOnClickListener { onEdit(user) }
        holder.binding.btnDelete.setOnClickListener { onDelete(user) }
    }
}