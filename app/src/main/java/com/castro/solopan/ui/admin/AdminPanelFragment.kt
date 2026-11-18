package com.castro.solopan.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.R
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.UserProfile
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.FragmentAdminPanelBinding
import com.castro.solopan.databinding.ItemPendingBinding
import com.castro.solopan.ui.auth.LoginActivity
import kotlinx.coroutines.launch

/**
 * Panel principal del Administrador: estadísticas de usuarios (leídas de Firestore),
 * acceso al reporte de ventas, solicitudes pendientes y cierre de sesión.
 */
class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvPending.layoutManager = LinearLayoutManager(requireContext())
        binding.btnSalesReport.setOnClickListener {
            startActivity(Intent(requireContext(), SalesReportActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            UserRepository.logout()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
        refreshPending()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    /** Cuenta compradores y panaderías reales en Firestore. */
    private fun loadStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val users = UserRepository.listAll()
                binding.tvStatBakeries.text = users.count { it.role == UserProfile.ROLE_SELLER }.toString()
                binding.tvStatBuyers.text = users.count { it.role == UserProfile.ROLE_BUYER }.toString()
            } catch (e: Exception) {
                // Si falla la consulta, las cifras quedan como estaban
            }
            binding.tvStatPqr.text = AdminStore.pqrs.count { !it.resolved }.toString()
        }
    }

    /** Muestra las solicitudes pendientes (de ejemplo hasta implementar la validación real). */
    private fun refreshPending() {
        binding.rvPending.adapter = PendingAdapter(AdminStore.pending.toList()) { item, approved ->
            AdminStore.pending.remove(item)
            val message = if (approved) R.string.msg_approved else R.string.msg_rejected
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            refreshPending()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Adaptador de solicitudes de registro pendientes, con botones de aprobar y rechazar. */
class PendingAdapter(
    private val items: List<String>,
    private val onDecision: (String, Boolean) -> Unit
) : RecyclerView.Adapter<PendingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPendingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemPendingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvPendingText.text = item
        holder.binding.btnApprove.setOnClickListener { onDecision(item, true) }
        holder.binding.btnReject.setOnClickListener { onDecision(item, false) }
    }
}