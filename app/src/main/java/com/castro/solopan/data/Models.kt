package com.castro.solopan.data

import java.text.NumberFormat
import java.util.Locale

data class Bakery(val name: String, val info: String, val hours: String)

data class Product(
    val id: Int,
    val bakeryName: String,
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val ingredients: String = "Harina, mantequilla, huevo, azúcar, sal",
    val allergens: String = "Gluten, lácteos, huevo"
)

data class CartItem(val product: Product, var quantity: Int)

fun Int.toCop(): String =
    "\$ " + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(this)

object SampleData {
    val bakeries = listOf(
        Bakery("Panadería La Espiga", "Pan, pasteles · 0,8 km · 20 min", "Abierto hasta 8:00 p. m."),
        Bakery("Pan de Barrio", "Croissants, almojábanas · 1,4 km", "Abierto hasta 7:00 p. m."),
        Bakery("Horno Dorado", "Tortas, roscones · 2,1 km", "Abierto hasta 6:30 p. m.")
    )

    val products = listOf(
        Product(1, "Panadería La Espiga", "Croissant de mantequilla", "Hojaldrado, horneado hoy", 4500, 24),
        Product(2, "Panadería La Espiga", "Pan integral", "Con semillas, 500 g", 8900, 12),
        Product(3, "Panadería La Espiga", "Roscón de guayaba", "Relleno dulce, unidad", 3800, 10),
        Product(4, "Panadería La Espiga", "Torta de chocolate", "Porción individual", 7200, 8),
        Product(5, "Pan de Barrio", "Almojábana x6", "Queso fresco, horneadas hoy", 9600, 15),
        Product(6, "Pan de Barrio", "Pan de bono", "Unidad, calientico", 2800, 30),
        Product(7, "Horno Dorado", "Torta de zanahoria", "Porción con crema", 8500, 9),
        Product(8, "Horno Dorado", "Roscón de arequipe", "Relleno de arequipe", 3500, 14)
    )
}

object CartManager {
    private val items = linkedMapOf<Int, CartItem>()

    fun add(product: Product) {
        val current = items[product.id]
        if (current == null) items[product.id] = CartItem(product, 1) else current.quantity++
    }

    fun increase(id: Int) {
        items[id]?.let { if (it.quantity < it.product.stock) it.quantity++ }
    }

    fun decrease(id: Int) {
        items[id]?.let { if (it.quantity > 1) it.quantity-- else items.remove(id) }
    }

    fun remove(id: Int) { items.remove(id) }
    fun clear() { items.clear() }
    fun items(): List<CartItem> = items.values.toList()
    fun subtotal(): Int = items.values.sumOf { it.product.price * it.quantity }
}

data class Order(val id: Int, val type: String, val summary: String, val total: Int, var status: String)
data class Invoice(val number: String, val detail: String, val total: Int)

object SellerStore {
    const val BAKERY = "Panadería La Espiga"
    val statuses = listOf("Nuevo", "En preparación", "Listo", "Entregado")

    val products = SampleData.products.filter { it.bakeryName == BAKERY }.toMutableList()

    val orders = mutableListOf(
        Order(1042, "Domicilio", "2 croissants, 1 roscón · Cra 15 #93-20", 15800, "Nuevo"),
        Order(1041, "Recoger", "3 panes integrales", 26700, "En preparación"),
        Order(1040, "Domicilio", "1 torta de chocolate", 10200, "Entregado")
    )

    val invoices = listOf(
        Invoice("FE-1042", "13 oct 2026 · María F. Rojas", 15800),
        Invoice("FE-1041", "13 oct 2026 · Carlos Peña", 26700),
        Invoice("FE-1040", "12 oct 2026 · Laura Gómez", 10200)
    )

    fun find(id: Int) = products.firstOrNull { it.id == id }
    fun nextId() = (products.maxOfOrNull { it.id } ?: 0) + 100

    fun upsert(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) products[index] = product else products.add(product)
    }

    fun delete(id: Int) { products.removeAll { it.id == id } }
}