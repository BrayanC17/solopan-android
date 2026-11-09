package com.castro.solopan.ui.seller

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.castro.solopan.R
import com.castro.solopan.data.Product
import com.castro.solopan.data.SellerStore
import com.castro.solopan.databinding.ActivityProductFormBinding

class ProductFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductFormBinding
    private var editingId = NEW_PRODUCT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getIntExtra(EXTRA_ID, NEW_PRODUCT)
        SellerStore.find(editingId)?.let {
            binding.etName.setText(it.name)
            binding.etDescription.setText(it.description)
            binding.etPrice.setText(it.price.toString())
            binding.etStock.setText(it.stock.toString())
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnPhoto.setOnClickListener {
            Toast.makeText(this, R.string.msg_photo_soon, Toast.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull()
        val stock = binding.etStock.text.toString().toIntOrNull()

        if (name.isBlank()) { binding.etName.error = getString(R.string.error_required); return }
        if (price == null) { binding.etPrice.error = getString(R.string.error_required); return }
        if (stock == null) { binding.etStock.error = getString(R.string.error_required); return }

        val id = if (editingId == NEW_PRODUCT) SellerStore.nextId() else editingId
        SellerStore.upsert(
            Product(
                id, SellerStore.BAKERY, name,
                binding.etDescription.text.toString().trim(), price, stock
            )
        )
        Toast.makeText(this, R.string.msg_product_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_ID = "extra_product_id"
        private const val NEW_PRODUCT = -1
    }
}