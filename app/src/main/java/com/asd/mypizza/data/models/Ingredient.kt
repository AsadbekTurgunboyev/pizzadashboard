package com.asd.mypizza.data.models
data class Ingredient(
    val ingredientId: String,
    val name: String,
    val price: Double,
    val isAvailable: Boolean = true
)
