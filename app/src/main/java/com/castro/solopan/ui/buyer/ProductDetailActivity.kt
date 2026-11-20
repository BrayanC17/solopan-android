package com.castro.solopan.ui.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.ProductRepository
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ActivityProductDetailBinding
import com.castro.solopan.util.ImageUtils
import kotlinx.coroutines.launch

/** Detalle de un producto: foto, precio, ingredientes, alérgenos y botón de agregar al carrito. */
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        loadProduct(intent.getStringExtra(EXTRA_ID).orEmpty())
    }

    /** Lee el producto de Firestore y llena la pantalla. */
    private fun loadProduct(id: String) {
        lifecycleScope.launch {
            try {
                val product = ProductRepository.get(id)
                if (product == null) {
                    finish()
                    return@launch
                }
                binding.tvName.text = product.name
                binding.tvBakery.text = product.bakeryName
                binding.tvPrice.text = product.price.toCop()
                binding.tvDescription.text = product.description
                binding.tvIngredients.text = product.ingredients.ifBlank { "-" }
                binding.tvAllergens.text = product.allergens.ifBlank { "-" }
                binding.tvStock.text = getString(R.string.detail_stock, product.stock)
                ImageUtils.show(binding.ivPhoto, product.imageBase64)

                binding.btnAdd.isEnabled = product.stock > 0
                binding.btnAdd.setOnClickListener {
                    CartManager.add(product)
                    Toast.makeText(this@ProductDetailActivity, R.string.msg_added_to_cart, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailActivity, R.string.msg_products_error, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_product_id"
    }
}