package com.ssafy.smartstore.dto

import java.io.Serializable

data class OrderMap(
    val completed: String,
    val img: String,
    val name: String,
    val o_id: Int,
    val order_time: String,
    val p_id: Int,
    val price: Int,
    val quantity: Int,
    val type: String,
    val user_id: String
) : Serializable{
    var others : Int = 0

    constructor(completed: String, img: String, name: String, o_id: Int, order_time: String, p_id: Int,
    price: Int, quantity: Int, type: String, user_id: String, others: Int = 0): this(completed, img, name, o_id, order_time, p_id, price, quantity, type, user_id)
}