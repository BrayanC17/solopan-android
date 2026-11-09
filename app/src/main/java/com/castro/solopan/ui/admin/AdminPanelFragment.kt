package com.castro.solopan.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.R
import com.castro.solopan.data.AdminStore
import com.castro.solopan.databinding.FragmentAdminPanelBinding
import com.castro.solopan.databinding.ItemPendingBinding

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
        refresh()
    }

    private fun refresh() {
        binding.tvStatBakeries.text =
            AdminStore.users.count { it.role == "Panadería" }.toString()
        binding.tvStatBuyers.text =
            AdminStore.users.count { it.role == "Comprador" }.toString()
        binding.tvStatPqr.text = AdminStore.pqrs.count { !it.resolved }.toString()
        binding.rvPending.adapter = PendingAdapter(AdminStore.pending.toList()) { item, approved ->
            AdminStore.pending.remove(item)
            val message = if (approved) R.string.msg_approved else R.string.msg_rejected
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

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