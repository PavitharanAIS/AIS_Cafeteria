package com.examples.aiscafeteria.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.util.ArrayList

class OrderDetailsModel() : Parcelable {
    var userUid : String ?= null
    var userName : String ?= null
    var foodNames : MutableList<String> ?= null
    var foodImages : MutableList<String> ?= null
    var foodPrices : MutableList<String> ?= null
    var foodQuantities : MutableList<Int> ?= null
    var address : String ?= null
    var totalPrice : String ?= null
    var phoneNumber : String ?= null
    var orderAccepted : Boolean = false
    var paymentReceived : Boolean = false
    var itemPushKey : String ?= null
    var currentTime : Long = 0

    constructor(parcel: Parcel) : this() {
        userUid = parcel.readString()
        userName = parcel.readString()
        foodNames = parcel.createStringArrayList()
        foodPrices = parcel.createStringArrayList()
        foodImages = parcel.createStringArrayList()
        foodQuantities = mutableListOf<Int>().apply {
            val size = parcel.readInt()
            for (i in 0 until size) {
                add(parcel.readInt())
            }
        }

        Log.d("OrderDetailsModel", "Food Names: $foodNames")
        Log.d("OrderDetailsModel", "Food Prices: $foodPrices")
        Log.d("OrderDetailsModel", "Food Images: $foodImages")
        Log.d("OrderDetailsModel", "Food Quantities: $foodQuantities")

        address = parcel.readString()
        totalPrice = parcel.readString()
        phoneNumber = parcel.readString()
        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        itemPushKey = parcel.readString()
        currentTime = parcel.readLong()
    }

    constructor(
        userId: String,
        name: String,
        foodItemName: ArrayList<String>,
        foodItemPrice: ArrayList<String>,
        foodItemImage: ArrayList<String>,
        foodItemQuantities: ArrayList<Int>,
        address: String,
        totalAmount: String,
        phone: String,
        time: Long,
        itemPushKey: String?,
        b: Boolean,
        b1: Boolean
    ) : this() {
        this.userUid = userId
        this.userName = name
        this.foodNames = foodItemName
        this.foodPrices = foodItemPrice
        this.foodImages = foodItemImage
        this.foodQuantities = foodItemQuantities
        this.address = address
        this.totalPrice = totalAmount
        this.phoneNumber = phone
        this.currentTime = time
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeStringList(foodNames)
        parcel.writeStringList(foodPrices)
        parcel.writeStringList(foodImages)
        parcel.writeInt(foodQuantities?.size ?: 0)
        foodQuantities?.forEach { parcel.writeInt(it) }
        parcel.writeString(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(itemPushKey)
        parcel.writeLong(currentTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetailsModel> {
        override fun createFromParcel(parcel: Parcel): OrderDetailsModel {
            return OrderDetailsModel(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetailsModel?> {
            return arrayOfNulls(size)
        }
    }
}