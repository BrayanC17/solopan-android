package com.castro.solopan.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.ProductRepository
import com.castro.solopan.databinding.ActivityBakeryBinding
import kotlinx.coroutines.launch

/** Catálogo de una panadería: productos activos leídos de Firestore. */
class BakeryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBakeryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBakeryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvBakeryTitle.text = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.btnBack.setOnClickListener { finish() }
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        loadProducts(intent.getStringExtra(EXTRA_UID).orEmpty())
    }

    /** Carga los productos activos de la panadería y arma la lista. */
    private fun loadProducts(ownerUid: String) {
        lifecycleScope.launch {
            try {
                val products = ProductRepository.listByOwner(ownerUid, onlyActive = true)
                if (products.isEmpty()) {
                    Toast.makeText(this@BakeryActivity, R.string.msg_no_products, Toast.LENGTH_SHORT).show()
                }
                binding.rvProducts.adapter = ProductAdapter(
                    products,
                    onAdd = { product ->
                        CartManager.add(product)
                        Toast.makeText(this@BakeryActivity, R.string.msg_added_to_cart, Toast.LENGTH_SHORT).show()
                    },
                    onItemClick = { product ->
                        startActivity(
                            Intent(this@BakeryActivity, ProductDetailActivity::class.java)
                                .putExtra(ProductDetailActivity.EXTRA_ID, product.id)
                        )
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@BakeryActivity, R.string.msg_products_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_NAME = "extra_bakery_name"
        const val EXTRA_UID = "extra_bakery_uid"
    }
}