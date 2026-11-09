package com.castro.solopan.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.ReportedProduct
import com.castro.solopan.databinding.FragmentAdminProductsBinding
import com.castro.solopan.databinding.ItemReportedBinding

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvReported.layoutManager = LinearLayoutManager(requireContext())
        binding.toggleTab.addOnButtonCheckedListener { _, _, _ -> refresh() }
        refresh()
    }

    private fun refresh() {
        val showHidden = binding.btnHidden.isChecked
        val list = AdminStore.reported.filter { it.hidden == showHidden }
        binding.rvReported.adapter = ReportedAdapter(
            list,
            onKeep = { AdminStore.reported.remove(it); refresh() },
            onHide = { it.hidden = true; refresh() }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ReportedAdapter(
    private val items: List<ReportedProduct>,
    private val onKeep: (ReportedProduct) -> Unit,
    private val onHide: (ReportedProduct) -> Unit
) : RecyclerView.Adapter<ReportedAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemReportedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemReportedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvProductName.text = item.name
        holder.binding.tvProductInfo.text = item.bakery
        holder.binding.tvReason.text = "Reportado: ${item.reason}"
        holder.binding.btnKeep.setOnClickListener { onKeep(item) }
        holder.binding.btnHide.setOnClickListener { onHide(item) }
        holder.binding.btnHide.isEnabled = !item.hidden
    }
}