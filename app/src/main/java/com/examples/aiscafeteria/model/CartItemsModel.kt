package com.examples.aiscafeteria.model

data class CartItemsModel(
    val foodName: String ?= null,
    val foodPrice: String ?= null,
    val foodDescription: String ?= null,
    val foodImage: String ?= null,
    val foodQuantity: Int ?= null,
    val foodIngredient: String ?= null
)


