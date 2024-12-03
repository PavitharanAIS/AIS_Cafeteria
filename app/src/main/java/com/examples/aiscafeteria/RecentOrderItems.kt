package com.examples.aiscafeteria

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.examples.aiscafeteria.adapter.RecentBuyAdapter
import com.examples.aiscafeteria.databinding.ActivityRecentOrderItemsBinding
import com.examples.aiscafeteria.model.OrderDetailsModel

class RecentOrderItems : AppCompatActivity() {

    private val binding : ActivityRecentOrderItemsBinding by lazy {
        ActivityRecentOrderItemsBinding.inflate(layoutInflater)
    }

    private lateinit var allFoodNames : ArrayList<String>
    private lateinit var allFoodImages : ArrayList<String>
    private lateinit var allFoodPrices : ArrayList<String>
    private lateinit var allFoodQuantities : ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.recentBuyBackButton.setOnClickListener {
            finish()
        }

        val recentOrderItems : OrderDetailsModel? = intent.getParcelableExtra("RecentBuyOrderItem")

        if (recentOrderItems != null) {
            Log.d("RecentOrderItem", "Received order details: $recentOrderItems")
        } else {
            Log.w("RecentOrderItem", "No recent order item received.")
        }

        recentOrderItems?.let { orderDetails ->
            allFoodNames = ArrayList(orderDetails.foodNames ?: emptyList())
            allFoodImages = ArrayList(orderDetails.foodImages ?: emptyList())
            allFoodPrices = ArrayList(orderDetails.foodPrices ?: emptyList())
            allFoodQuantities = ArrayList(orderDetails.foodQuantities ?: emptyList())
        } ?: run {
            // Handle the case when there is no order details
            allFoodNames = ArrayList()
            allFoodImages = ArrayList()
            allFoodPrices = ArrayList()
            allFoodQuantities = ArrayList()
        }

        Log.d("RecentOrderItems", "Food Names: $allFoodNames")
        Log.d("RecentOrderItems", "Food Images: $allFoodImages")
        Log.d("RecentOrderItems", "Food Prices: $allFoodPrices")
        Log.d("RecentOrderItems", "Food Quantities: $allFoodQuantities")

        setAdapter()

    }

    private fun setAdapter() {
        val rv = binding.recentListRecyclerView
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this, allFoodNames, allFoodImages, allFoodPrices, allFoodQuantities)
        rv.adapter = adapter
    }
}