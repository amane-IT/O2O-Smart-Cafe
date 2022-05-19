package com.ssafy.smartstore.dto

import java.io.Serializable

data class OrderDetail(var orderId: Int, var productId: Int, val quantity: Int = 1): Serializable
