package com.examples.aiscafeteria.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.examples.aiscafeteria.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private var cartDescriptions: MutableList<String>,
    private var cartImages: MutableList<String>,
    private var cartQuantity: MutableList<Int>,
    private var cartIngredient: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        itemQuantities = cartQuantity.toIntArray()
        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }

    companion object {
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartItemsReference: DatabaseReference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    fun getUpdatedItemsQuantities(): MutableList<Int> {
        return cartQuantity.toMutableList()
    }

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                val quantity = itemQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]
                cartItemQuantity.text = quantity.toString()

                // Load image using Glide
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)

                minusButton.setOnClickListener { decreaseQuantity(position) }
                plusButton.setOnClickListener { increaseQuantity(position) }
                deleteButton.setOnClickListener { deleteItem(position) }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun deleteItem(position: Int) {
            if (position in 0 until cartItems.size) { // Validate position
                getUniqueKeyAtPosition(position) { uniqueKey ->
                    if (uniqueKey != null) {
                        removeItem(position, uniqueKey)
                    } else {
                        Toast.makeText(context, "Failed to find item in database.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Invalid position.", Toast.LENGTH_SHORT).show()
            }
        }


        private fun removeItem(position: Int, uniqueKey: String) {
            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                if (position in 0 until cartItems.size) { // Check list bounds again
                    try {
                        // Safely remove from all lists
                        cartItems.removeAt(position)
                        cartItemPrices.removeAt(position)
                        cartDescriptions.removeAt(position)
                        cartImages.removeAt(position)
                        cartQuantity.removeAt(position)
                        cartIngredient.removeAt(position)

                        // Update itemQuantities to reflect new cart state
                        itemQuantities = cartQuantity.toIntArray()

                        // Notify adapter of item removal
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, cartItems.size)

                        Toast.makeText(context, "Item deleted.", Toast.LENGTH_SHORT).show()
                    } catch (e: IndexOutOfBoundsException) {
                        Log.e("CartAdapter", "Error removing item: ${e.message}")
                    }
                } else {
                    Toast.makeText(context, "Item already removed.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete item.", Toast.LENGTH_SHORT).show()
            }
        }



        private fun getUniqueKeyAtPosition(position: Int, onComplete: (String?) -> Unit) {
            cartItemsReference.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val keys = snapshot.children.map { it.key }
                    if (position in keys.indices) {
                        onComplete(keys[position])
                    } else {
                        onComplete(null) // Invalid position
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartAdapter", "Failed to fetch key: ${error.message}")
                    onComplete(null)
                }
            })
        }

    }
}





