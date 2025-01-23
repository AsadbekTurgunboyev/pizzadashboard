package com.asd.mypizza.data.models

data class Order(
    val orderId: String,
    val customerName: String,
    val items: List<OrderItem>,
    val totalPrice: Double,
    val status: String,
    val timestamp: Long
)

data class OrderItem(
    val pizzaName: String,
    val qty: Int,
    val price: Double,
    val customIngredients: List<String> = listOf()
)
