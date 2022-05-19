package com.ssafy.smartstore.dto

data class Stamp(
    val id: Int,
    val orderId: Int,
    var quantity: Int,
    val userId: String
)