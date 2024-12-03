package com.examples.aiscafeteria.model

data class MenuItemModel(
    val foodName : String ?= null,
    val foodPrice : String ?= null,
    val foodDescription : String ?= null,
    val foodImage : String ?= null,
    val foodIngredient : String ?= null
)
