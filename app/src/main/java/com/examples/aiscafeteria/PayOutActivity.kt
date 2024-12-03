package com.examples.aiscafeteria

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.examples.aiscafeteria.databinding.ActivityPayOutBinding
import com.examples.aiscafeteria.model.OrderDetailsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.math.BigDecimal
import java.math.RoundingMode

class PayOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase and user details
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Set user data
        setUserData()

        // Get order details from the intent
        val intent = intent
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: arrayListOf()
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: arrayListOf()
        foodItemIngredient = intent.getStringArrayListExtra("FoodItemIngredient") ?: arrayListOf()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: arrayListOf()

        try {
            totalAmount = "$" + calculateTotalAmount().toString()
            if (totalAmount.endsWith(".0")) totalAmount = totalAmount.removeSuffix(".0")
            binding.payoutTotalAmount.isEnabled = false
            binding.payoutTotalAmount.setText(totalAmount)
        } catch (e: Exception) {
            Log.e("PayOutActivity", "Error calculating total amount", e)
            Toast.makeText(this, "Error calculating total amount. Please try again.", Toast.LENGTH_SHORT).show()
        }

        binding.payOutBackButton.setOnClickListener {
            finish()
        }

        binding.placeOrderButton.setOnClickListener {
            name = binding.payoutName.text.toString().trim()
            address = binding.payoutAddress.text.toString().trim()
            phone = binding.payoutPhone.text.toString().trim()

            if (validateInputs(name, address, phone)) {
                placeOrder()
            }
        }
    }

    private fun validateInputs(name: String, address: String, phone: String): Boolean {
        return when {
            name.isBlank() -> {
                showToast("Name cannot be empty.")
                false
            }
            address.isBlank() -> {
                showToast("Address cannot be empty.")
                false
            }
            phone.isBlank() -> {
                showToast("Phone number cannot be empty.")
                false
            }
            !phone.matches(Regex("^[0-9]{10,15}\$")) -> {
                showToast("Enter a valid phone number.")
                false
            }
            else -> true
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: run {
            showToast("User not logged in. Please log in and try again.")
            return
        }

        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key ?: run {
            showToast("Unable to generate order key. Please try again.")
            return
        }

        val orderDetails = OrderDetailsModel(
            userId, name, foodItemName, foodItemPrice, foodItemImage,
            foodItemQuantities, address, totalAmount, phone, time, itemPushKey, false, false
        )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey)
        orderReference.setValue(orderDetails)
            .addOnSuccessListener {
                val bottomSheetDialog = CongratsBottomSheet()
                bottomSheetDialog.show(supportFragmentManager, "Test")
                removeItemFromCart()
                addOrderToHistory(orderDetails)
            }
            .addOnFailureListener { e ->
                showToast("Failed to place order. Please try again.")
                Log.e("PayOutActivity", "Error placing order", e)
            }
    }


    private fun addOrderToHistory(orderDetails: OrderDetailsModel) {
        databaseReference.child("user").child(userId).child("OrderHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails)
            .addOnFailureListener { e ->
                Log.e("PayOutActivity", "Error adding order to history", e)
                showToast("Failed to add order to history.")
            }
    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue().addOnFailureListener { e ->
            Log.e("PayOutActivity", "Error removing items from cart", e)
            showToast("Failed to remove items from cart.")
        }
    }

    private fun calculateTotalAmount(): Double {
        var totalAmount = 0.00
        for (i in foodItemPrice.indices) {
            try {
                val price = foodItemPrice[i].removePrefix("$").toDouble()
                val quantity = foodItemQuantities.getOrNull(i)?.toDouble() ?: 1.0
                totalAmount += price * quantity
            } catch (e: NumberFormatException) {
                Log.e("PayOutActivity", "Invalid price format for ${foodItemPrice[i]}", e)
                throw e
            }
        }
        return BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val names = snapshot.child("name").getValue(String::class.java) ?: ""
                        val addresses = snapshot.child("address").getValue(String::class.java) ?: ""
                        val phones = snapshot.child("phone").getValue(String::class.java) ?: ""

                        binding.apply {
                            payoutName.setText(names)
                            payoutAddress.setText(addresses)
                            payoutPhone.setText(phones)
                        }
                    } else {
                        showToast("User data not found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PayOutActivity", "Error fetching user data", error.toException())
                    showToast("Failed to fetch user data. Please try again.")
                }
            })
        } else {
            showToast("User not logged in. Please log in and try again.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

