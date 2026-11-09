package com.castro.solopan.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.Product
import com.castro.solopan.data.SellerStore
import com.castro.solopan.databinding.FragmentSellerProductsBinding

class SellerProductsFragment : Fragment() {

    private var _binding: FragmentSellerProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.btnNew.setOnClickListener { openForm(NEW_PRODUCT) }
    }

    override fun onResume() {
        super.onResume()
        binding.rvProducts.adapter = SellerProductAdapter(
            SellerStore.products.toList(),
            onEdit = { openForm(it.id) },
            onDelete = { confirmDelete(it) }
        )
    }

    private fun openForm(id: Int) {
        startActivity(
            Intent(requireContext(), ProductFormActivity::class.java)
                .putExtra(ProductFormActivity.EXTRA_ID, id)
        )
    }

    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_product_title)
            .setMessage(R.string.dialog_delete_product_message)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                SellerStore.delete(product.id)
                onResume()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val NEW_PRODUCT = -1
    }
}