package com.castro.solopan.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.R
import com.castro.solopan.data.Bakery
import com.castro.solopan.data.ProductRepository
import com.castro.solopan.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

/** Inicio del Comprador: lista las panaderías registradas y permite buscarlas por nombre. */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /** Panaderías cargadas desde Firestore; la búsqueda filtra esta copia local. */
    private var bakeries: List<Bakery> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvBakeries.layoutManager = LinearLayoutManager(requireContext())
        binding.etSearch.doAfterTextChanged { render() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                bakeries = ProductRepository.listBakeries()
                render()
                if (bakeries.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.msg_no_bakeries, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(requireContext(), R.string.msg_products_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Muestra las panaderías cuyo nombre contiene el texto buscado. */
    private fun render() {
        if (_binding == null) return
        val query = binding.etSearch.text.toString().trim().lowercase()
        val list = bakeries.filter { it.name.lowercase().contains(query) }
        binding.rvBakeries.adapter = BakeryAdapter(list) { bakery ->
            startActivity(
                Intent(requireContext(), BakeryActivity::class.java)
                    .putExtra(BakeryActivity.EXTRA_UID, bakery.uid)
                    .putExtra(BakeryActivity.EXTRA_NAME, bakery.name)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}