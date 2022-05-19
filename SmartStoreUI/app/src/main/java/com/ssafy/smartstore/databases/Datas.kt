package com.ssafy.smartstore.databases

import java.io.Serializable

// product: img, name, price
// order_detail: quantity
data class Shopping(var img: String, var name: String, var price: Int, var quantity: Int):Serializable
data class History(var img: String, var name: String, var price: Int, var quantity: Int, var order_time: Long, var qty: Int):Serializable
