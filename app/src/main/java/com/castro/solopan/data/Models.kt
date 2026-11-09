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
    val stock: Int
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
    fun items(): List<CartItem> = items.values.toList()
    fun subtotal(): Int = items.values.sumOf { it.product.price * it.quantity }
}