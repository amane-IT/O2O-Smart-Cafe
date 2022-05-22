package com.ssafy.smartstore.fragment

import java.io.Serializable

// product: img, name, price
// order_detail: quantity
data class Shopping(var img: String, var name: String, var price: Int, var quantity: Int):Serializable{
    var id: Int = 0

    constructor(id: Int, img: String, name: String, price: Int, quantity: Int): this(img, name, price, quantity){
        this.id = id
    }
}