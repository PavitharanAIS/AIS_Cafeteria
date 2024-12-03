package com.examples.aiscafeteria.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.examples.aiscafeteria.PayOutActivity
import com.examples.aiscafeteria.adapter.CartAdapter
import com.examples.aiscafeteria.databinding.FragmentCartBinding
import com.examples.aiscafeteria.model.CartItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImagesUri: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var quantity: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        retrieveCartItems()

        binding.proceedButton.setOnClickListener { getOrderItemsDetail() }
        return binding.root
    }

    private fun getOrderItemsDetail() {
        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()

        // Create an Intent to start PayOutActivity
        val intent = Intent(requireContext(), PayOutActivity::class.java)

        // Pass the cart details as extras
        intent.putStringArrayListExtra("FoodItemName", ArrayList(foodNames))
        intent.putStringArrayListExtra("FoodItemPrice", ArrayList(foodPrices))
        intent.putStringArrayListExtra("FoodItemImage", ArrayList(foodImagesUri))
        intent.putStringArrayListExtra("FoodItemDescription", ArrayList(foodDescriptions))
        intent.putStringArrayListExtra("FoodItemIngredient", ArrayList(foodIngredients))
        intent.putIntegerArrayListExtra("FoodItemQuantities", ArrayList(foodQuantities))

        // Start PayOutActivity
        startActivity(intent)
    }

    private fun retrieveCartItems() {
        val foodReference = database.reference.child("user").child(auth.currentUser?.uid ?: "").child("CartItems")
        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        quantity = mutableListOf()

        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val cartItem = foodSnapshot.getValue(CartItemsModel::class.java)
                    cartItem?.apply {
                        foodName?.let { foodNames.add(it) }
                        foodPrice?.let { foodPrices.add(it) }
                        foodDescription?.let { foodDescriptions.add(it) }
                        foodImage?.let { foodImagesUri.add(it) }
                        foodQuantity?.let { quantity.add(it) }
                        foodIngredient?.let { foodIngredients.add(it) }
                    }
                }
                setAdapter()
            }

            private fun setAdapter() {
                cartAdapter = CartAdapter(
                    requireContext(), foodNames, foodPrices, foodDescriptions, foodImagesUri, quantity, foodIngredients
                )
                binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.cartRecyclerView.adapter = cartAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cart items.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}