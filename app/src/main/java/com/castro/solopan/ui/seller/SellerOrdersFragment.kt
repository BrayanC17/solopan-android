package com.castro.solopan.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.data.SellerStore
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.FragmentSellerOrdersBinding

class SellerOrdersFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvOrdersCount.text = SellerStore.orders.size.toString()
        binding.tvSales.text = SellerStore.orders.sumOf { it.total }.toCop()
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = OrderAdapter(SellerStore.orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}