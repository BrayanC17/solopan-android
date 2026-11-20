package com.castro.solopan.data

import java.text.NumberFormat
import java.util.Locale

/** Panadería visible para el Comprador (viene de un usuario con rol vendedor). */
data class Bakery(val uid: String, val name: String, val owner: String, val hours: String)

/**
 * Producto guardado en Firestore (colección "products").
 * Todos los campos tienen valor por defecto para que Firestore pueda crear el objeto.
 * La imagen se guarda como texto Base64 (JPEG reducido).
 */
data class Product(
    val id: String = "",
    val ownerUid: String = "",
    val bakeryName: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val ingredients: String = "",
    val allergens: String = "",
    val imageBase64: String = "",
    val active: Boolean = true
) {
    /** Convierte el producto a un mapa para escribirlo en Firestore. */
    fun toMap(): Map<String, Any> = mapOf(
        "ownerUid" to ownerUid,
        "bakeryName" to bakeryName,
        "name" to name,
        "description" to description,
        "price" to price,
        "stock" to stock,
        "ingredients" to ingredients,
        "allergens" to allergens,
        "imageBase64" to imageBase64,
        "active" to active
    )
}

data class CartItem(val product: Product, var quantity: Int)

/** Da formato de pesos colombianos: 4500 -> "$ 4.500". */
fun Int.toCop(): String =
    "\$ " + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(this)

/** Carrito en memoria. La clave es el id del producto en Firestore. */
object CartManager {
    private val items = linkedMapOf<String, CartItem>()

    fun add(product: Product) {
        val current = items[product.id]
        if (current == null) items[product.id] = CartItem(product, 1)
        else if (current.quantity < product.stock) current.quantity++
    }

    fun increase(id: String) {
        items[id]?.let { if (it.quantity < it.product.stock) it.quantity++ }
    }

    fun decrease(id: String) {
        items[id]?.let { if (it.quantity > 1) it.quantity-- else items.remove(id) }
    }

    fun remove(id: String) { items.remove(id) }
    fun clear() { items.clear() }
    fun items(): List<CartItem> = items.values.toList()
    fun subtotal(): Int = items.values.sumOf { it.product.price * it.quantity }
}

data class Order(val id: Int, val type: String, val summary: String, val total: Int, var status: String)
data class Invoice(val number: String, val detail: String, val total: Int)

/** Pedidos y facturas de ejemplo del Vendedor (se reemplazan al implementar las órdenes). */
object SellerStore {
    val statuses = listOf("Nuevo", "En preparación", "Listo", "Entregado")

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
}

data class AppUser(
    val id: Int,
    var name: String,
    var email: String,
    var role: String,
    var active: Boolean
)

data class Pqr(
    val id: Int,
    val type: String,
    val detail: String,
    val due: String,
    var resolved: Boolean
)

data class SalesRow(val bakery: String, val total: Int)

data class ReportedProduct(
    val id: Int,
    val name: String,
    val bakery: String,
    val reason: String,
    var hidden: Boolean
)

/** Datos de ejemplo del Administrador (PQR, reportes y solicitudes) hasta implementarlos en Firestore. */
object AdminStore {
    val users = mutableListOf(
        AppUser(1, "María Fernanda Rojas", "mfernanda@correo.com", "Comprador", true),
        AppUser(2, "Panadería La Espiga", "laespiga@correo.com", "Panadería", true),
        AppUser(3, "Carlos Peña", "cpena@correo.com", "Comprador", false),
        AppUser(4, "Horno Dorado", "hornodorado@correo.com", "Panadería", true)
    )

    val pending = mutableListOf(
        "Panadería El Trigal · Solicitud de registro (NIT 900.123.456-7)",
        "Panadería Dulce Sabor · Solicitud de registro (NIT 901.222.333-1)"
    )

    val reported = mutableListOf(
        ReportedProduct(1, "Almojábana x6", "Pan de Barrio", "Descripción engañosa", false),
        ReportedProduct(2, "Roscón de guayaba", "Panadería La Espiga", "Precio sin IVA", false)
    )

    val pqrs = mutableListOf(
        Pqr(318, "Queja", "Pedido #1038 no fue entregado", "Vence en 2 días", false),
        Pqr(317, "Petición", "Reembolso por empaque dañado", "Vence en 9 días", false),
        Pqr(315, "Reclamo", "Cobro duplicado en la pasarela", "Vence en 12 días", false)
    )

    val sales = listOf(
        SalesRow("Panadería La Espiga", 4186500),
        SalesRow("Pan de Barrio", 2310000),
        SalesRow("Horno Dorado", 1745200)
    )
}