package com.asd.mypizza.data.models

data class Pizza(
    val name: String = "",
    val imageUrl: String = "",
    val sizes: Map<String, Double> = emptyMap(),
    val ingredients: List<IngredientItem> = emptyList()
)

data class IngredientItem(
    val name: String = "",
    val price: Int = 0
)
