package com.castro.solopan.ui.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.SampleData
import com.castro.solopan.databinding.ActivityBakeryBinding
import android.content.Intent

class BakeryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBakeryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBakeryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bakeryName = intent.getStringExtra(EXTRA_NAME).orEmpty()
        binding.tvBakeryTitle.text = bakeryName
        binding.btnBack.setOnClickListener { finish() }

        val products = SampleData.products.filter { it.bakeryName == bakeryName }
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = ProductAdapter(
            products,
            onAdd = { product ->
                CartManager.add(product)
                Toast.makeText(this, R.string.msg_added_to_cart, Toast.LENGTH_SHORT).show()
            },
            onItemClick = { product ->
                startActivity(
                    Intent(this, ProductDetailActivity::class.java)
                        .putExtra(ProductDetailActivity.EXTRA_ID, product.id)
                )
            }
        )
    }

    companion object {
        const val EXTRA_NAME = "extra_bakery_name"
    }
}