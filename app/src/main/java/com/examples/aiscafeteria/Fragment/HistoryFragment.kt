package com.examples.aiscafeteria.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.examples.aiscafeteria.R
import com.examples.aiscafeteria.RecentOrderItems
import com.examples.aiscafeteria.adapter.BuyAgainAdapter
import com.examples.aiscafeteria.databinding.FragmentHistoryBinding
import com.examples.aiscafeteria.databinding.RecentBuyItemBinding
import com.examples.aiscafeteria.model.OrderDetailsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private var listOfOrderItem: MutableList<OrderDetailsModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        //initialize firebase auth and database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //Retrieve and display the user order history
        retrieveOrderHistory()

        binding.recentBuyCard.setOnClickListener {
            seeItemsRecentBuy()
        }

        binding.buyAgainReceivedButton.setOnClickListener {
            updateOrderStatus()
        }

        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey!!)
        completeOrderReference.child("paymentReceived").setValue(true)
    }

    private fun seeItemsRecentBuy() {

//        listOfOrderItem.firstOrNull()?.let { recentBuy ->
//            val intent = Intent(requireContext(), RecentOrderItems::class.java)
//            intent.putExtra("RecentBuyOrderItem", listOfOrderItem)
//            startActivity(intent)
//        }

        if (listOfOrderItem.isNotEmpty()) {
            val recentOrderItem = listOfOrderItem.first() // Get the most recent item
            Log.d("HistoryFragment", "ListItem: $recentOrderItem")

            val intent = Intent(requireContext(), RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem", recentOrderItem) // Pass the single order item

            startActivity(intent)
        } else {
            Log.d("HistoryFragment", "No recent buy items found.")
        }
    }

    private fun retrieveOrderHistory() {
        binding.recentBuyCard.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""

        val buyItemReference: DatabaseReference =
            database.reference.child("user").child(userId).child("OrderHistory")
        val sortingQuery = buyItemReference.orderByChild("currentTime")

        sortingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetailsModel::class.java)
                    buyHistoryItem?.let {
                        Log.d("HistoryFragment", "Retrieved Order - User: ${it.userName}, Foods: ${it.foodNames}, Prices: ${it.foodPrices}, Images: ${it.foodImages}")
                        listOfOrderItem.add(it)
                        Log.d("HistoryFragment", "ArrayListOfItems: $listOfOrderItem")

                    }
                }
                listOfOrderItem.reverse()

                if (listOfOrderItem.isNotEmpty()) {
                    setDataInRecentBuyItem()
                    setPreviousBuyItemsRecyclerView()
                } else {
                    Log.d("HistoryFragment", "No order items found.")
                }
            }

            private fun setDataInRecentBuyItem() {
                binding.recentBuyCard.visibility = View.VISIBLE
                val recentOrderItem = listOfOrderItem.firstOrNull()
                recentOrderItem?.let {
                    Log.d("HistoryFragment", "Setting data for recent buy item: $it") // Log the recent order item

                    with(binding) {
                        buyAgainFoodName.text = it.foodNames?.firstOrNull() ?: ""
                        buyAgainFoodPrice.text = it.foodPrices?.firstOrNull() ?: ""

                        val image = it.foodImages?.firstOrNull() ?: ""
                        val uri = Uri.parse(image)
                        Glide.with(requireContext()).load(uri).into(buyAgainFoodImage)

                        val isOrderIsAccepted = listOfOrderItem[0].orderAccepted
                        Log.d("History Fragment","orderAccepted: ${isOrderIsAccepted}")
                        if (isOrderIsAccepted) {
                            buyAgainOrderStatus.background.setTint(Color.GREEN)
                            buyAgainReceivedButton.visibility = View.VISIBLE
                        }

                        Log.d("HistoryFragment", "BuyAgainFoodName: ${buyAgainFoodName.text}, BuyAgainFoodPrice: ${buyAgainFoodPrice.text}, BuyAgainFoodImage: ${buyAgainFoodImage}") // Log the recent order item
                    }
                }
            }

            private fun setPreviousBuyItemsRecyclerView() {

                val buyAgainFoodName = mutableListOf<String>()
                val buyAgainFoodPrice = mutableListOf<String>()
                val buyAgainFoodImage = mutableListOf<String>()

                for (i in 0 until listOfOrderItem.size) {
                    listOfOrderItem[i].foodNames?.firstOrNull()?.let {
                        buyAgainFoodName.add(it)
                        Log.d("HistoryFragment", "Buy Again Food Names: $buyAgainFoodName")

                    }
                    listOfOrderItem[i].foodPrices?.firstOrNull()?.let {
                        buyAgainFoodPrice.add(it)
                        Log.d("HistoryFragment", "Buy Again Food Prices: $buyAgainFoodPrice")

                    }
                    listOfOrderItem[i].foodImages?.firstOrNull()?.let {
                        buyAgainFoodImage.add(it)
                        Log.d("HistoryFragment", "Buy Again Food Images: $buyAgainFoodImage")

                    }

                }
                Log.d("HistoryFragment", "Buy Again Food Names: $buyAgainFoodName")
                Log.d("HistoryFragment", "Buy Again Food Prices: $buyAgainFoodPrice")
                Log.d("HistoryFragment", "Buy Again Food Images: $buyAgainFoodImage")

                val rv = binding.BuyAgainRecyclerView
                rv.layoutManager = LinearLayoutManager(requireContext())
                buyAgainAdapter = BuyAgainAdapter(
                    buyAgainFoodName,
                    buyAgainFoodPrice,
                    buyAgainFoodImage,
                    requireContext()
                )
                rv.adapter = buyAgainAdapter
                Log.d("HistoryFragment", "Adapter item count: ${buyAgainAdapter.itemCount}")
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}