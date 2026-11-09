package com.castro.solopan.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.castro.solopan.R
import com.castro.solopan.data.AdminStore
import com.castro.solopan.data.Pqr
import com.castro.solopan.databinding.FragmentPqrBinding
import com.castro.solopan.databinding.ItemPqrBinding

class PqrFragment : Fragment() {

    private var _binding: FragmentPqrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPqrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvPqr.layoutManager = LinearLayoutManager(requireContext())
        binding.toggleTab.addOnButtonCheckedListener { _, _, _ -> refresh() }
        refresh()
    }

    private fun refresh() {
        val showResolved = binding.btnResolved.isChecked
        val list = AdminStore.pqrs.filter { it.resolved == showResolved }
        binding.rvPqr.adapter = PqrAdapter(list) {
            it.resolved = true
            Toast.makeText(requireContext(), R.string.msg_pqr_resolved, Toast.LENGTH_SHORT).show()
            refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PqrAdapter(
    private val items: List<Pqr>,
    private val onResolve: (Pqr) -> Unit
) : RecyclerView.Adapter<PqrAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPqrBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemPqrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvPqrTitle.text = "PQR #${item.id} · ${item.type}"
        holder.binding.tvPqrDue.text = item.due
        holder.binding.tvPqrDetail.text = item.detail
        holder.binding.btnResolve.visibility = if (item.resolved) View.GONE else View.VISIBLE
        holder.binding.btnResolve.setOnClickListener { onResolve(item) }
    }
}