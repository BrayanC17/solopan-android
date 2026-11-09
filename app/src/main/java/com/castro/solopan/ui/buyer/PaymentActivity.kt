package com.castro.solopan.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.castro.solopan.R
import com.castro.solopan.data.CartManager
import com.castro.solopan.data.toCop
import com.castro.solopan.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val total = intent.getIntExtra(EXTRA_TOTAL, 0)
        binding.tvSummary.text = CartManager.items()
            .joinToString("\n") { "${it.quantity} × ${it.product.name}" }
        binding.tvTotal.text = "${total.toCop()} COP"
        binding.btnPay.text = getString(R.string.btn_pay, total.toCop())

        binding.btnBack.setOnClickListener { finish() }
        binding.rgMethod.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutCard.visibility = if (checkedId == R.id.rbCard) View.VISIBLE else View.GONE
        }
        binding.btnPay.setOnClickListener { validateAndPay() }
    }

    private fun validateAndPay() {
        if (binding.rbCard.isChecked) {
            if (binding.etCard.text.toString().length != CARD_LENGTH) {
                binding.etCard.error = getString(R.string.error_card)
                return
            }
            if (!Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(binding.etExpiry.text.toString())) {
                binding.etExpiry.error = getString(R.string.error_expiry)
                return
            }
            if (binding.etCvv.text.toString().length != CVV_LENGTH) {
                binding.etCvv.error = getString(R.string.error_cvv)
                return
            }
        }
        val reference = "SP-" + (100000..999999).random()
        AlertDialog.Builder(this)
            .setTitle(R.string.pay_success_title)
            .setMessage(getString(R.string.pay_success_message, reference))
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                CartManager.clear()
                finish()
            }
            .show()
    }

    companion object {
        const val EXTRA_TOTAL = "extra_total"
        private const val CARD_LENGTH = 16
        private const val CVV_LENGTH = 3
    }
}