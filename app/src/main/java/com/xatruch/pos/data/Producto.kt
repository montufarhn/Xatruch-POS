package com.xatruch.pos.data

enum class TipoProducto {
    PLATILLO, BEBIDA
}

data class Producto(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nombre: String,
    var precio: Double,
    var tipo: TipoProducto
)