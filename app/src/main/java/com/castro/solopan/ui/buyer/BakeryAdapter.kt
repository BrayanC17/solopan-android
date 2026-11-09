package com.castro.solopan.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.Bakery
import com.castro.solopan.databinding.ItemBakeryBinding

class BakeryAdapter(
    private val bakeries: List<Bakery>,
    private val onClick: (Bakery) -> Unit
) : RecyclerView.Adapter<BakeryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBakeryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBakeryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = bakeries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bakery = bakeries[position]
        holder.binding.tvBakeryName.text = bakery.name
        holder.binding.tvBakeryInfo.text = bakery.info
        holder.binding.tvBakeryHours.text = bakery.hours
        holder.binding.root.setOnClickListener { onClick(bakery) }
    }
}