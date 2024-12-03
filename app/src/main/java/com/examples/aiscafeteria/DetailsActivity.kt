package com.examples.aiscafeteria

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.examples.aiscafeteria.databinding.ActivityDetailsBinding
import com.examples.aiscafeteria.model.CartItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding

    private var foodName: String? = null
    private var foodImage: String? = null
    private var foodDescription: String? = null
    private var foodIngredient: String? = null
    private var foodPrice: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        //initialize firebase auth
        auth = FirebaseAuth.getInstance()

        foodName = intent.getStringExtra("MenuItemName")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodIngredient = intent.getStringExtra("MenuItemIngredients")
        foodImage = intent.getStringExtra("MenuItemImage")
        foodPrice = intent.getStringExtra("MenuItemPrice")

        with(binding) {
            detailFoodName.text = foodName
            descriptionTextView.text = foodDescription
            ingredientTextView.text = foodIngredient
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)
        }

        binding.detailimageButton.setOnClickListener {
            finish()
        }

        binding.detailsAddToCartButton.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid?:""
        //create a cart item object
        val cartItem = CartItemsModel(foodName.toString(), foodPrice.toString(), foodDescription.toString(), foodImage.toString(), 1)

        //save data to cart item to firebase database
            database.child("user").child(userId).child("CartItems").push().setValue(cartItem).addOnSuccessListener {
                Toast.makeText(this, "Items added to cart successfully.", Toast.LENGTH_SHORT).show()
            } .addOnFailureListener {
                Toast.makeText(this, "Failed to add items to cart.", Toast.LENGTH_SHORT).show()

            }
    }
}