package com.examples.aiscafeteria.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.examples.aiscafeteria.databinding.RecentBuyItemBinding

class RecentBuyAdapter(
    private var context: Context,
    private var foodNameList: ArrayList<String>,
    private var foodImageList: ArrayList<String>,
    private var foodPriceList: ArrayList<String>,
    private var foodQuantityList: ArrayList<Int>
) : RecyclerView.Adapter<RecentBuyAdapter.RecentBuyViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentBuyAdapter.RecentBuyViewHolder {
        val binding = RecentBuyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentBuyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentBuyAdapter.RecentBuyViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodNameList.size

    inner class RecentBuyViewHolder(private val binding: RecentBuyItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
                binding.apply {
                    recentListFoodName.text = foodNameList[position]
                    Log.d("RecentBuyAdapter", "Food name: ${recentListFoodName.text}")
                    recentListFoodQuantity.text = foodQuantityList[position].toString()
                    Log.d("RecentBuyAdapter", "Food Quantity: ${recentListFoodQuantity.text}")

                    recentListFoodPrice.text = foodPriceList[position]
                    Log.d("RecentBuyAdapter", "Food price: ${recentListFoodPrice.text}")

                    val uriString = foodImageList[position]
                    val uri = Uri.parse(uriString)
                    Glide.with(context).load(uri).into(recentListFoodImage)
                    Log.d("RecentBuyAdapter", "Food Image: ${recentListFoodImage}")

                }

        }

    }
}