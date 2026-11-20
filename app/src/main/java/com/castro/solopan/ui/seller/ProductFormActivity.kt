package com.castro.solopan.ui.seller

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.castro.solopan.R
import com.castro.solopan.data.Product
import com.castro.solopan.data.ProductRepository
import com.castro.solopan.data.UserRepository
import com.castro.solopan.databinding.ActivityProductFormBinding
import com.castro.solopan.ui.auth.RoleRouter
import com.castro.solopan.util.ImageUtils
import kotlinx.coroutines.launch

/**
 * Formulario para crear o editar un producto del Vendedor, con foto tomada con la cámara
 * o elegida de la galería. El producto se guarda en Firestore asociado al vendedor.
 */
class ProductFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductFormBinding

    /** Id del producto que se edita; null cuando se crea uno nuevo. */
    private var editingId: String? = null

    /** Foto actual en Base64 (vacía si el producto no tiene foto). */
    private var imageBase64 = ""

    /** Abre la cámara y recibe una vista previa de la foto (no requiere permiso de cámara). */
    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) setImage(ImageUtils.bitmapToBase64(bitmap))
        }

    /** Abre la galería y recibe la imagen elegida. */
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val data = ImageUtils.uriToBase64(this@ProductFormActivity, uri)
                    if (data == null) {
                        Toast.makeText(this@ProductFormActivity, R.string.msg_photo_error, Toast.LENGTH_SHORT).show()
                    } else {
                        setImage(data)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingId = intent.getStringExtra(EXTRA_ID)
        editingId?.let { loadProduct(it) }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnPhoto.setOnClickListener { chooseSource() }
        binding.btnSave.setOnClickListener { save() }
    }

    /** Carga en el formulario los datos del producto que se va a editar. */
    private fun loadProduct(id: String) {
        lifecycleScope.launch {
            try {
                val product = ProductRepository.get(id)
                if (product == null) {
                    finish()
                    return@launch
                }
                binding.etName.setText(product.name)
                binding.etDescription.setText(product.description)
                binding.etIngredients.setText(product.ingredients)
                binding.etAllergens.setText(product.allergens)
                binding.etPrice.setText(product.price.toString())
                binding.etStock.setText(product.stock.toString())
                binding.swAvailable.isChecked = product.active
                setImage(product.imageBase64)
            } catch (e: Exception) {
                Toast.makeText(this@ProductFormActivity, R.string.msg_products_error, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Pregunta si la foto viene de la cámara o de la galería. */
    private fun chooseSource() {
        AlertDialog.Builder(this)
            .setTitle(R.string.photo_source_title)
            .setItems(
                arrayOf(getString(R.string.photo_camera), getString(R.string.photo_gallery))
            ) { _, which ->
                if (which == 0) takePhoto.launch(null) else pickImage.launch("image/*")
            }
            .show()
    }

    private fun setImage(data: String) {
        imageBase64 = data
        ImageUtils.show(binding.ivPhoto, data)
    }

    /** Valida los campos y guarda el producto en Firestore. */
    private fun save() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull()
        val stock = binding.etStock.text.toString().toIntOrNull()

        if (name.isBlank()) { binding.etName.error = getString(R.string.error_required); return }
        if (price == null || price < 0) { binding.etPrice.error = getString(R.string.error_required); return }
        if (stock == null || stock < 0) { binding.etStock.error = getString(R.string.error_required); return }

        binding.btnSave.isEnabled = false
        lifecycleScope.launch {
            try {
                val seller = UserRepository.fetchCurrent()
                ProductRepository.save(
                    Product(
                        id = editingId.orEmpty(),
                        ownerUid = seller.uid,
                        bakeryName = seller.bakeryName.ifBlank { seller.name },
                        name = name,
                        description = binding.etDescription.text.toString().trim(),
                        price = price,
                        stock = stock,
                        ingredients = binding.etIngredients.text.toString().trim(),
                        allergens = binding.etAllergens.text.toString().trim(),
                        imageBase64 = imageBase64,
                        active = binding.swAvailable.isChecked
                    )
                )
                Toast.makeText(this@ProductFormActivity, R.string.msg_product_saved, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProductFormActivity, RoleRouter.messageFor(e), Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_product_id"
    }
}