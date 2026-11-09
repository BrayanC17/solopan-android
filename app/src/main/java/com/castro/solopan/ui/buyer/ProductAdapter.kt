package com.castro.solopan.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.Product
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ItemProductBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onAdd: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvProductName.text = product.name
        holder.binding.tvProductDescription.text = product.description
        holder.binding.tvProductPrice.text = product.price.toCop()
        holder.binding.btnAdd.setOnClickListener { onAdd(product) }
    }
}