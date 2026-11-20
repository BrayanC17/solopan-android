package com.castro.solopan.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.Product
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ItemProductBinding
import com.castro.solopan.util.ImageUtils

/** Lista de productos de una panadería: foto, precio, botón de agregar y detalle al tocar. */
class ProductAdapter(
    private val products: List<Product>,
    private val onAdd: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
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
        ImageUtils.show(holder.binding.ivPhoto, product.imageBase64)
        // Sin stock no se puede agregar al carrito
        holder.binding.btnAdd.isEnabled = product.stock > 0
        holder.binding.btnAdd.setOnClickListener { onAdd(product) }
        holder.binding.root.setOnClickListener { onItemClick(product) }
    }
}