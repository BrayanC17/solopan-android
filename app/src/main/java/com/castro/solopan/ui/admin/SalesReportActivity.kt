package com.castro.solopan.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.SalesRow
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ActivitySalesReportBinding
import com.castro.solopan.databinding.ItemInvoiceBinding

class SalesReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvTotal.text = AdminStore.sales.sumOf { it.total }.toCop()
        binding.rvSales.layoutManager = LinearLayoutManager(this)
        binding.rvSales.adapter = SalesAdapter(AdminStore.sales)
    }
}

class SalesAdapter(private val rows: List<SalesRow>) :
    RecyclerView.Adapter<SalesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInvoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = rows.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]
        holder.binding.tvNumber.text = row.bakery
        holder.binding.tvDetail.text = row.total.toCop()
    }
}