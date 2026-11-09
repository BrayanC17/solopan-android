package com.castro.solopan.ui.buyer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ItemCartBinding

class CartAdapter(private val onChanged: () -> Unit) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private var data = CartManager.items()

    class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val id = item.product.id
        holder.binding.tvCartName.text = item.product.name
        holder.binding.tvCartPrice.text = (item.product.price * item.quantity).toCop()
        holder.binding.tvQty.text = item.quantity.toString()
        holder.binding.btnPlus.setOnClickListener { CartManager.increase(id); refresh() }
        holder.binding.btnMinus.setOnClickListener { CartManager.decrease(id); refresh() }
        holder.binding.btnDelete.setOnClickListener { CartManager.remove(id); refresh() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refresh() {
        data = CartManager.items()
        notifyDataSetChanged()
        onChanged()
    }
}