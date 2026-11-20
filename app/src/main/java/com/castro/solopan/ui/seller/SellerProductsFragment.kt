package com.castro.solopan.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.Product
import com.castro.solopan.data.ProductRepository
import com.castro.solopan.databinding.FragmentSellerProductsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/** Lista de productos del Vendedor con sesión abierta: ver, crear, editar y eliminar. */
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
        binding.btnNew.setOnClickListener { openForm(null) }
    }

    /** Recarga desde Firestore al volver del formulario. */
    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = ProductRepository.listByOwner(uid)
                binding.rvProducts.adapter = SellerProductAdapter(
                    products,
                    onEdit = { openForm(it.id) },
                    onDelete = { confirmDelete(it) }
                )
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(requireContext(), R.string.msg_products_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openForm(id: String?) {
        val intent = Intent(requireContext(), ProductFormActivity::class.java)
        id?.let { intent.putExtra(ProductFormActivity.EXTRA_ID, it) }
        startActivity(intent)
    }

    /** Pide confirmación y elimina el producto de Firestore. */
    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_product_title)
            .setMessage(R.string.dialog_delete_product_message)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        ProductRepository.delete(product.id)
                        Toast.makeText(requireContext(), R.string.msg_product_deleted, Toast.LENGTH_SHORT).show()
                        load()
                    } catch (e: Exception) {
                        if (_binding != null) {
                            Toast.makeText(requireContext(), R.string.msg_generic_error, Toast.LENGTH_SHORT).show()
                        }
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