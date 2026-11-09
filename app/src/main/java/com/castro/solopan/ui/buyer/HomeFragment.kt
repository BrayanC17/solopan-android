package com.castro.solopan.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.castro.solopan.data.SampleData
import com.castro.solopan.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvBakeries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBakeries.adapter = BakeryAdapter(SampleData.bakeries) { bakery ->
            val intent = Intent(requireContext(), BakeryActivity::class.java)
                .putExtra(BakeryActivity.EXTRA_NAME, bakery.name)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}