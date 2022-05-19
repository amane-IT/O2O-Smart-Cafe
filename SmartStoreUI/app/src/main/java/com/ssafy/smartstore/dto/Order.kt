package com.ssafy.smartstore.dto

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

val currentDateTime = Calendar.getInstance().time
var dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(currentDateTime)

data class Order(var userId: String, var orderTable: String? = null, var order_time: String = dateFormat, var completed: Char = 'N'): Serializable{
    var o_id: Int? = null
    var detail: String? = null
    var stamp: Int? = null
    var details: List<OrderDetail>? = null

    constructor(id: Int, userId: String, orderTable: String?, order_time: String, completed: Char, detail: String, stamp:Int): this(userId, orderTable, order_time, completed){
        this.o_id = id
        this.detail = detail
        this.stamp = stamp
    }

    constructor(userId: String, orderTable: String?, order_time: String, completed: Char, details: List<OrderDetail>): this(userId, orderTable, order_time, completed){
        this.details = details
    }

}
