package com.castro.solopan.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = CartAdapter { updateTotals() }
        binding.toggleDelivery.addOnButtonCheckedListener { _, _, _ -> updateTotals() }
        binding.btnCheckout.setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_payment_soon, Toast.LENGTH_SHORT).show()
        }
        updateTotals()
    }

    private fun updateTotals() {
        val isEmpty = CartManager.items().isEmpty()
        val delivery = if (binding.btnDelivery.isChecked && !isEmpty) DELIVERY_COST else 0
        val subtotal = CartManager.subtotal()

        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.btnCheckout.isEnabled = !isEmpty
        binding.tvSubtotal.text = subtotal.toCop()
        binding.tvDeliveryCost.text = delivery.toCop()
        binding.tvTotal.text = (subtotal + delivery).toCop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DELIVERY_COST = 3000
    }
}