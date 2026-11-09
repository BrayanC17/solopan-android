package com.castro.solopan.ui.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.Order
import com.castro.solopan.data.SellerStore
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ItemOrderBinding

class OrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val b = holder.binding
        b.tvOrderTitle.text = "Pedido #${order.id} · ${order.type}"
        b.tvOrderSummary.text = "${order.summary} · ${order.total.toCop()}"
        b.tvOrderStatus.text = order.status
        val isLast = order.status == SellerStore.statuses.last()
        b.btnAdvance.visibility = if (isLast) View.GONE else View.VISIBLE
        b.btnAdvance.setOnClickListener {
            val next = SellerStore.statuses.indexOf(order.status) + 1
            order.status = SellerStore.statuses[next]
            notifyItemChanged(holder.bindingAdapterPosition)
        }
    }
}