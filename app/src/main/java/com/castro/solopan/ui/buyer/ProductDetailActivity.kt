package com.castro.solopan.ui.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.SampleData
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val product = SampleData.products.firstOrNull { it.id == intent.getIntExtra(EXTRA_ID, -1) }
        if (product == null) {
            finish()
            return
        }
        binding.tvName.text = product.name
        binding.tvBakery.text = product.bakeryName
        binding.tvPrice.text = product.price.toCop()
        binding.tvDescription.text = product.description
        binding.tvIngredients.text = product.ingredients
        binding.tvAllergens.text = product.allergens
        binding.tvStock.text = getString(R.string.detail_stock, product.stock)

        binding.btnAdd.setOnClickListener {
            CartManager.add(product)
            Toast.makeText(this, R.string.msg_added_to_cart, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_ID = "extra_product_id"
    }
}