package com.castro.solopan.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.data.SellerStore
import com.castro.solopan.data.UserRepository
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.FragmentSellerOrdersBinding
import kotlinx.coroutines.launch

/**
 * Panel de pedidos del Vendedor. El título muestra el nombre real de su panadería,
 * leído de su perfil en Firestore. Los pedidos siguen siendo de ejemplo hasta
 * implementar el flujo de órdenes.
 */
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
        loadBakeryName()
    }

    /** Muestra el nombre de la panadería del usuario con sesión abierta. */
    private fun loadBakeryName() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profile = UserRepository.fetchCurrent()
                binding.tvPanelTitle.text = profile.bakeryName.ifBlank { profile.name }
            } catch (e: Exception) {
                // Si falla la lectura, el título queda vacío; no se bloquea la pantalla
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}