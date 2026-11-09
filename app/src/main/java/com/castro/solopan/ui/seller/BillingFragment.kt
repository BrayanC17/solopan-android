package com.castro.solopan.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.Invoice
import com.castro.solopan.data.SellerStore
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.FragmentBillingBinding
import com.castro.solopan.databinding.ItemInvoiceBinding

class BillingFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvBilled.text = SellerStore.invoices.sumOf { it.total }.toCop()
        binding.rvInvoices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInvoices.adapter = InvoiceAdapter(SellerStore.invoices)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class InvoiceAdapter(private val invoices: List<Invoice>) :
    RecyclerView.Adapter<InvoiceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInvoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = invoices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.binding.tvNumber.text = "${invoice.number} · ${invoice.total.toCop()}"
        holder.binding.tvDetail.text = invoice.detail
    }
}