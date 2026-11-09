package com.castro.solopan.ui.seller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.castro.solopan.R
import com.castro.solopan.databinding.ActivitySellerBinding
import com.castro.solopan.ui.buyer.ProfileFragment

class SellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_products -> SellerProductsFragment()
                R.id.nav_billing -> BillingFragment()
                R.id.nav_profile -> buildProfileFragment()
                else -> SellerOrdersFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
        if (savedInstanceState == null) binding.bottomNav.selectedItemId = R.id.nav_orders
    }

    private fun buildProfileFragment(): Fragment {
        val role = getString(R.string.role_seller)
        val name = getString(R.string.seller_name)
        return ProfileFragment().apply {
            arguments = bundleOf(
                ProfileFragment.ARG_ROLE to role,
                ProfileFragment.ARG_NAME to name
            )
        }
    }
}